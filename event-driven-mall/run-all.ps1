Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# ============================================================================
# 🛒 EVENT-DRIVEN MALL - 1-Click Startup (Windows)
# - Sobe infra via Docker Compose
# - Abre 1 terminal por serviço (APIs, workers e frontend)
# - Cria um drive SUBST (ex: M:\) pra evitar bug de acento no Lein/JVM no Windows
# ============================================================================

function Write-Section([string]$title) {
  Write-Host ""
  Write-Host "======================================================" -ForegroundColor DarkGray
  Write-Host $title -ForegroundColor Cyan
  Write-Host "======================================================" -ForegroundColor DarkGray
}

function Resolve-ProjectRoot {
  return (Resolve-Path $PSScriptRoot).Path
}

function Ensure-TempDir {
  $temp = "C:\temp"
  if (-not (Test-Path $temp)) {
    New-Item -ItemType Directory -Path $temp | Out-Null
  }
}

function Get-DockerComposeCmd {
  # Preferência: "docker compose" (v2). Fallback: "docker-compose" (v1).
  try {
    docker compose version | Out-Null
    return @{ Kind = "docker compose"; Cmd = { param([string[]]$args) docker compose @args } }
  } catch {
    return @{ Kind = "docker-compose"; Cmd = { param([string[]]$args) docker-compose @args } }
  }
}

function Wait-Port([string]$name, [int]$port, [int]$timeoutSeconds = 60) {
  $start = Get-Date
  while ($true) {
    $ok = $false
    try {
      $ok = (Test-NetConnection -ComputerName "localhost" -Port $port -WarningAction SilentlyContinue).TcpTestSucceeded
    } catch { $ok = $false }

    if ($ok) {
      Write-Host "✅ $name pronto na porta $port" -ForegroundColor Green
      return
    }

    if (((Get-Date) - $start).TotalSeconds -ge $timeoutSeconds) {
      Write-Host "⚠️  Timeout aguardando $name na porta $port (continuando mesmo assim)" -ForegroundColor Yellow
      return
    }

    Start-Sleep -Seconds 2
  }
}

function Ensure-SubstDrive([string]$driveLetter, [string]$targetPath) {
  # Se o drive já existir, não mexemos (evita derrubar drive do usuário)
  $existing = (Get-PSDrive -Name $driveLetter -ErrorAction SilentlyContinue)
  $drive = "$driveLetter`:"
  if ($existing) {
    return @{ Root = "$drive\"; Created = $false; Drive = $drive }
  }

  subst $drive $targetPath | Out-Null
  return @{ Root = "$drive\"; Created = $true; Drive = $drive }
}

function Start-ServiceTerminal {
  param(
    [Parameter(Mandatory=$true)][string]$name,
    [Parameter(Mandatory=$true)][string]$path,
    [Parameter(Mandatory=$true)][string]$command
  )

  $psCmd = @"
`$host.UI.RawUI.WindowTitle = '$name'
Set-Location -LiteralPath '$path'
$command
"@

  Start-Process -FilePath "powershell.exe" -ArgumentList @(
    "-NoProfile",
    "-ExecutionPolicy", "Bypass",
    "-NoExit",
    "-Command", $psCmd
  ) -PassThru
}

$projectRoot = Resolve-ProjectRoot
Write-Section "🚀 Iniciando Event-Driven Mall"
Write-Host "📁 Projeto: $projectRoot" -ForegroundColor Gray

Ensure-TempDir

Write-Section "🏗️ Subindo Infra (Docker Compose)"
$dc = Get-DockerComposeCmd
Write-Host "🐳 Usando: $($dc.Kind)" -ForegroundColor Gray

Push-Location $projectRoot
try {
  & $dc.Cmd @("up","-d","--remove-orphans")
} finally {
  Pop-Location
}

Write-Host "⏳ Aguardando serviços essenciais..." -ForegroundColor Gray
Wait-Port "Kafka" 9092 90
Wait-Port "MongoDB" 27018 90
Wait-Port "PostgreSQL" 5433 90

Write-Section "🧭 Preparando caminho ASCII (SUBST) pra evitar bug de acento"
$substInfo = Ensure-SubstDrive "M" $projectRoot
$ROOT = $substInfo.Root
Write-Host "📌 Root usado para execução: $ROOT" -ForegroundColor Gray

$stateDir = Join-Path $projectRoot ".run"
if (-not (Test-Path $stateDir)) { New-Item -ItemType Directory -Path $stateDir | Out-Null }
$statePath = (Join-Path $stateDir "state.json")

Write-Section "🧩 Abrindo terminais (1 por serviço)"

# Vamos registrar PIDs para permitir stop-all encerrar tudo automaticamente.
$pids = [ordered]@{}

# Java (Spring Boot) — no Windows use o mvnw.cmd
$pids["Command API (8080)"] = (Start-ServiceTerminal "Command API (8080)" (Join-Path $ROOT "services\command-api") ".\mvnw.cmd spring-boot:run").Id
$pids["Query API (8081)"] = (Start-ServiceTerminal "Query API (8081)" (Join-Path $ROOT "services\query-api") ".\mvnw.cmd spring-boot:run").Id
$pids["Inventory Worker (8082)"] = (Start-ServiceTerminal "Inventory Worker (8082)" (Join-Path $ROOT "services\inventory-worker") ".\mvnw.cmd spring-boot:run").Id

# Clojure (Lein) — força TEMP/TMP ASCII
$leinEnv = '$env:TEMP="C:\temp"; $env:TMP="C:\temp";'
$pids["Payment Worker (Clojure)"] = (Start-ServiceTerminal "Payment Worker (Clojure)" (Join-Path $ROOT "services\payment-worker") "$leinEnv lein deps; lein run!").Id
$pids["Projection Worker (Clojure)"] = (Start-ServiceTerminal "Projection Worker (Clojure)" (Join-Path $ROOT "services\consulta-worker") "$leinEnv lein deps; lein run!").Id

# Frontend (Vite) — instala deps só se necessário
$frontendCmd = @'
if (-not (Test-Path ".\node_modules")) { npm ci }
npm run dev
'@
$pids["Frontend (Vite)"] = (Start-ServiceTerminal "Frontend (Vite)" (Join-Path $ROOT "services\frontend") $frontendCmd).Id

@{
  createdSubst = $substInfo.Created
  drive = $substInfo.Drive
  root = $ROOT
  timestamp = (Get-Date).ToString("o")
  pids = $pids
} | ConvertTo-Json -Depth 10 | Set-Content -Encoding UTF8 -Path $statePath

Write-Section "✅ Disparo concluído"
Write-Host "🌐 Kafka UI: http://localhost:8090" -ForegroundColor Gray
Write-Host "🛑 Para parar: duplo clique em stop-all.cmd (ou rode .\stop-all.ps1)" -ForegroundColor Gray


