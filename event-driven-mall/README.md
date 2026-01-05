# 🛒 Event-Driven Mall

Arquitetura Event-Driven com **CQRS** e **Event Sourcing** usando Kafka, PostgreSQL e MongoDB.

## 🎬 Demonstração

![Demo da Aplicação](demo.gif)

> **Architecture Debugger em Tempo Real**: Visualize o fluxo de eventos percorrendo toda a arquitetura, desde a criação de produtos até o processamento assíncrono pelos workers e atualização das projeções no MongoDB.

## 📐 Arquitetura

```
┌─────────────┐    POST /orders     ┌──────────────┐
│   React     │ ─────────────────► │  Command API │
│  Frontend   │    202 Accepted     │ (Java/Spring)│
└─────────────┘                     └──────┬───────┘
       │                                   │
       │ GET /orders/{id}                  │ Salva + Publica
       │                                   ▼
       │                           ┌──────────────┐
       │                           │  PostgreSQL  │ (Event Store)
       │                           └──────────────┘
       │                                   │
       │                                   ▼
       │                           ┌──────────────┐
       │                           │    Kafka     │ ◄──── orders
       │                           │  (Event Bus) │
       │                           └──────┬───────┘
       │                                   │
       │            ┌──────────────────────┼──────────────────────┐
       │            │                      │                      │
       │            ▼                      ▼                      ▼
       │   ┌────────────────┐    ┌────────────────┐    ┌────────────────┐
       │   │Inventory Worker│    │ Payment Worker │    │   Projector    │
       │   │    (Java)      │    │   (Clojure)    │    │   (Clojure)    │
       │   └───────┬────────┘    └───────┬────────┘    └───────┬────────┘
       │           │                     │                     │
       │           │ stock-reserved      │ payment-success     │
       │           └─────────────────────┴─────────────────────┘
       │                                                       │
       │                                               Upsert  ▼
       │                                           ┌──────────────┐
       │                                           │   MongoDB    │ (Read Model)
       │                                           └──────┬───────┘
       │                                                  │
       │                                                  ▼
       │                                           ┌──────────────┐
       └──────────────────────────────────────────►│  Query API   │
                                                   │ (Java/Spring)│
                                                   └──────────────┘
```

## 🚀 Como Rodar

### ✅ 0. Subir tudo com 1 clique (Windows)

- **Start**: dê duplo clique em `run-all.cmd`
- **Stop**: dê duplo clique em `stop-all.cmd`

Isso vai:
- subir a infraestrutura com `docker compose`
- abrir **1 terminal por serviço** (APIs, workers e frontend)
- criar um drive `SUBST` (ex: `M:\`) para evitar problemas de caminho com acento no Lein/JVM no Windows

### 1. Subir Infraestrutura (Docker)
```bash
cd event-driven-mall
docker compose up -d
```

Serviços disponíveis:
- **Kafka UI**: http://localhost:8090
- **PostgreSQL**: localhost:5433
- **MongoDB**: localhost:27018

### 2. Rodar os Serviços Java

**Terminal 1 - Command API (porta 8080):**
```bash
cd services/command-api
./mvnw spring-boot:run
```

**Terminal 2 - Inventory Worker (porta 8082):**
```bash
cd services/inventory-worker
./mvnw spring-boot:run
```

**Terminal 3 - Query API (porta 8081):**
```bash
cd services/query-api
./mvnw spring-boot:run
```

### 3. Rodar os Workers Clojure

**Terminal 4 - Payment Worker:**
```bash
cd services/payment-worker
lein trampoline run
# ou: lein run!
```

**Terminal 5 - Projection Worker:**
```bash
cd services/consulta-worker
lein trampoline run
# ou: lein run!
```

> Se no Windows aparecer `Could not find or load main class clojure.main`,
> garanta que o Leiningen está atualizado e que as dependências foram baixadas
> (`lein deps`). Este repo configura `:local-repo "C:/m2"` nos projetos Clojure
> para evitar problemas com caminhos com acento.
>
> Se aparecer erro com `form-init...clj` em `AppData\\Local\\Temp` (caminho corrompido por acento),
> crie `C:\\temp` e rode os workers com TEMP/TMP apontando para lá.
>
> Se o Lein ainda não achar o namespace (`Could not locate payment_worker/core.clj on classpath`),
> é quase sempre **caminho com acento** no diretório do projeto. No Windows, rode os workers por
> um caminho “ASCII” usando `subst`:
>
> ```powershell
> subst M: "C:\Users\João Pedro\Documents\PROJETOS LEGÍTIMOS"
> cd M:\event-driven-mall\services\payment-worker
> $env:TEMP="C:\temp"; $env:TMP="C:\temp"
> lein deps
> lein run!
> ```

### 4. Rodar o Frontend React

**Terminal 6 - Frontend (porta 3000):**
```bash
cd services/frontend
npm install
npm run dev
```

## 📡 Endpoints

### Command API (8080) - Escrita
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/orders` | Cria novo pedido |

### Query API (8081) - Leitura
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/orders/{id}` | Busca pedido por ID |
| GET | `/orders` | Lista todos os pedidos |
| GET | `/orders/user/{userId}` | Pedidos de um usuário |
| GET | `/orders/status/{status}` | Pedidos por status |

## 📨 Exemplo de Request

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "usuario_123",
    "totalAmount": 199.90,
    "items": ["Teclado Mecânico", "Mouse Gamer"]
  }'
```

## 🔄 Fluxo de Eventos

1. **OrderCreated** → Tópico `orders`
2. **StockReserved** → Tópico `stock-reserved`
3. **PaymentSuccess** → Tópico `payment-success`

## 🗃️ Bancos de Dados

- **PostgreSQL (Event Store)**: Armazena todos os eventos (append-only)
- **MongoDB (Read Model)**: Projeções otimizadas para consulta

## 📁 Estrutura de Pastas

```
event-driven-mall/
├── .gitignore
├── docker-compose.yml
├── README.md
└── services/
    ├── command-api/        # Java Spring Boot (Comandos)
    ├── query-api/          # Java Spring Boot (Consultas)
    ├── inventory-worker/   # Java Spring Boot (Estoque)
    ├── payment-worker/     # Clojure (Pagamento)
    ├── consulta-worker/    # Clojure (Projection Worker → MongoDB)
    │   ├── src/consulta_worker/
    │   │   ├── config.clj           # Configurações
    │   │   ├── core.clj             # Orquestração principal
    │   │   ├── kafka/               # Lógica Kafka
    │   │   │   └── consumer.clj
    │   │   ├── database/            # Camada de persistência
    │   │   │   ├── connection.clj
    │   │   │   └── repository.clj
    │   │   ├── projections/         # Lógica de projeções
    │   │   │   └── order_projection.clj
    │   │   ├── handlers/            # Roteamento de eventos
    │   │   │   └── event_handler.clj
    │   │   └── utils/               # Utilitários
    │   │       ├── time.clj
    │   │       └── json.clj
    │   └── project.clj
    └── frontend/           # React + Vite
```

## 🛠️ Tecnologias

| Serviço | Stack |
|---------|-------|
| Command API | Java 17, Spring Boot 4, Kafka, PostgreSQL |
| Query API | Java 17, Spring Boot 4, MongoDB |
| Inventory Worker | Java 17, Spring Boot 4, Kafka |
| Payment Worker | Clojure 1.11, Kafka Clients |
| Projector Worker | Clojure 1.11, Kafka Clients, Monger (MongoDB) |
| Frontend | React 18, Vite 5 |
| Message Broker | Apache Kafka + Zookeeper |
| Databases | PostgreSQL 15, MongoDB 6 |

