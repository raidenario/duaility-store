Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# ============================================================================
# 🛒 EVENT-DRIVEN MALL - 1-Click Shutdown (Windows)
# - Derruba infra via Docker Compose
# - Remove SUBST drive (somente se foi criado pelo run-all.ps1)
# ============================================================================

function Get-DockerComposeCmd {
  try {
    docker compose version | Out-Null
    return @{ Kind = "docker compose"; Cmd = { param([string[]]$args) docker compose @args } }
  } catch {
    return @{ Kind = "docker-compose"; Cmd = { param([string[]]$args) docker-compose @args } }
  }
}

$projectRoot = (Resolve-Path $PSScriptRoot).Path
Write-Host "🛑 Parando Event-Driven Mall..." -ForegroundColor Red
Write-Host "📁 Projeto: $projectRoot" -ForegroundColor Gray

# 0) Encerra terminais/serviços abertos pelo run-all (se possível)
$statePath = Join-Path $projectRoot ".run\state.json"
if (Test-Path $statePath) {
  try {
    $state = Get-Content -Raw -Path $statePath | ConvertFrom-Json
    if ($state.pids) {
      Write-Host "🧹 Encerrando processos iniciados pelo run-all..." -ForegroundColor Yellow
      foreach ($entry in $state.pids.PSObject.Properties) {
        $name = $entry.Name
        $pid = [int]$entry.Value
        if ($pid -gt 0) {
          try {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "✅ Encerrado: $name (PID $pid)" -ForegroundColor Green
          } catch {
            Write-Host "⚠️  Não consegui encerrar: $name (PID $pid)" -ForegroundColor Yellow
          }
        }
      }
    }
  } catch {
    # ignora falhas de parse
  }
}

# 1) Derruba Docker
$dc = Get-DockerComposeCmd
Write-Host "🐳 Usando: $($dc.Kind)" -ForegroundColor Gray

Push-Location $projectRoot
try {
  & $dc.Cmd @("down")
} finally {
  Pop-Location
}

# 2) Remove SUBST se tiver sido criado pelo run-all.ps1
if (Test-Path $statePath) {
  try {
    $state = Get-Content -Raw -Path $statePath | ConvertFrom-Json
    if ($state.createdSubst -eq $true -and $state.drive) {
      Write-Host "🧹 Removendo drive SUBST criado pelo run-all: $($state.drive)" -ForegroundColor Yellow
      subst $state.drive /D | Out-Null
    }
  } catch {
    # ignora falhas de parse
  }
}

Write-Host "✅ Infraestrutura (Docker) parada!" -ForegroundColor Green
Write-Host "💡 Se algum terminal ainda ficar aberto, pode fechar manualmente (ou ele pode já ter sido encerrado pelo Stop-Process)." -ForegroundColor Yellow


