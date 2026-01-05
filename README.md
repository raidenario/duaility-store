<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=28&duration=4000&pause=1000&color=58A6FF&center=true&vCenter=true&width=600&lines=%E2%98%AF%EF%B8%8F+Duality+Store;Event-Driven+Architecture;CQRS+%2B+Event+Sourcing" alt="Typing SVG" />
</p>

<p align="center">
  <strong>Uma simulação de e-commerce distribuída explorando a dualidade entre consistência transacional e performance de leitura através de CQRS, Event Sourcing e Arquitetura Poliglota.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Clojure-1.11-5881D8?style=for-the-badge&logo=clojure&logoColor=white" alt="Clojure"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Apache_Kafka-3.5-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white" alt="Kafka"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="Postgres"/>
  <img src="https://img.shields.io/badge/MongoDB-6.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
</p>

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#%EF%B8%8F-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Como Usar](#-como-usar)
- [API Reference](#-api-reference)
- [Fluxo de Eventos](#-fluxo-de-eventos)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Roadmap](#-roadmap)
- [Autor](#-autor)

---


## 🎯 Sobre o Projeto

O **Duality Store** é um projeto educacional e demonstrativo que implementa uma arquitetura de microsserviços moderna usando padrões avançados de design distribuído. O nome "Duality" reflete os dois pilares fundamentais do sistema:

## 🎬 Demonstração

![Demo da Aplicação](demo.gif)

> **Architecture Debugger em Tempo Real**: Visualize o fluxo de eventos percorrendo toda a arquitetura, desde a criação de produtos até o processamento assíncrono pelos workers e atualização das projeções no MongoDB.

### 🔄 Dualidade de Dados (CQRS)

Separação estrita entre:
- **Modelo de Escrita (Command)** → PostgreSQL como Event Store
- **Modelo de Leitura (Query)** → MongoDB com projeções otimizadas

### 🌐 Dualidade de Linguagem (Arquitetura Poliglota)

Uso estratégico de diferentes paradigmas:
- **Java/Spring Boot** → APIs e regras de negócio críticas (tipagem forte, transações ACID)
- **Clojure** → Workers de processamento de eventos (imutabilidade, programação funcional)

### 💡 Por que essa arquitetura?

| Problema Tradicional | Solução com Duality |
|---------------------|---------------------|
| Leituras e escritas competindo por recursos | CQRS separa os workloads |
| Perda de histórico de mudanças | Event Sourcing preserva todos os eventos |
| Acoplamento entre serviços | Kafka desacopla via mensagens assíncronas |
| Modelo único para tudo | Read Models otimizados para cada caso de uso |

---

## 🏗 Arquitetura

```
                                    ┌─────────────────────────────────────────────────────────────┐
                                    │                    EVENT-DRIVEN MALL                         │
                                    └─────────────────────────────────────────────────────────────┘

    ┌─────────────┐                                                                      
    │   React     │    POST /orders                                                      
    │  Frontend   │ ───────────────────►  ┌──────────────────┐                          
    │  (Vite)     │    202 Accepted       │   Command API    │                          
    └──────┬──────┘                       │  (Java/Spring)   │                          
           │                              └────────┬─────────┘                          
           │                                       │                                     
           │ GET /orders                           │ 1. Persiste Evento                  
           │                                       ▼                                     
           │                              ┌──────────────────┐                          
           │                              │   PostgreSQL     │  Event Store             
           │                              │  (Write Model)   │  (append-only)           
           │                              └────────┬─────────┘                          
           │                                       │                                     
           │                                       │ 2. Publica no Kafka                 
           │                                       ▼                                     
           │                              ┌──────────────────┐                          
           │                              │   Apache Kafka   │  Event Bus               
           │                              │   (Zookeeper)    │                          
           │                              └────────┬─────────┘                          
           │                                       │                                     
           │              ┌────────────────────────┼────────────────────────┐            
           │              │                        │                        │            
           │              ▼                        ▼                        ▼            
           │    ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐   
           │    │ Inventory Worker │    │  Payment Worker  │    │ Projector Worker │   
           │    │     (Java)       │    │    (Clojure)     │    │    (Clojure)     │   
           │    │                  │    │                  │    │                  │   
           │    │ • Reserva Stock  │    │ • Débito Wallet  │    │ • Consome Eventos│   
           │    │ • Valida Qtd     │    │ • Verifica Saldo │    │ • Projeta no DB  │   
           │    └────────┬─────────┘    └────────┬─────────┘    └────────┬─────────┘   
           │             │                       │                       │              
           │             │ stock-reserved        │ payment-success       │              
           │             └───────────────────────┴───────────────────────┘              
           │                                                             │              
           │                                                     Upsert  ▼              
           │                                              ┌──────────────────┐          
           │                                              │     MongoDB      │          
           │                                              │   (Read Model)   │          
           │                                              │  Projeções Opt.  │          
           │                                              └────────┬─────────┘          
           │                                                       │                    
           │                                                       ▼                    
           │                                              ┌──────────────────┐          
           └─────────────────────────────────────────────►│    Query API     │          
                                                          │  (Java/Spring)   │          
                                                          │  Alta Performance│          
                                                          └──────────────────┘          
```

### Componentes do Sistema

| Serviço | Linguagem | Responsabilidade |
|---------|-----------|------------------|
| **Command API** | Java/Spring | Recebe comandos, valida, persiste eventos e publica no Kafka |
| **Query API** | Java/Spring | Serve dados projetados do MongoDB com baixa latência |
| **Inventory Worker** | Java/Spring | Processa reserva de estoque, emite `stock-reserved` |
| **Payment Worker** | Clojure | Processa pagamentos, verifica saldo, emite `payment-success/failed` |
| **Projector Worker** | Clojure | Consome todos os eventos e materializa Read Models no MongoDB |
| **Frontend** | React/Vite | Interface visual com debugger de arquitetura em tempo real |

---

## 🛠️ Tecnologias

### Backend

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Java | 17 LTS | APIs e Workers Java |
| Spring Boot | 3.2 | Framework para APIs REST |
| Spring Kafka | 3.1 | Integração com Apache Kafka |
| Spring Data JPA | 3.2 | Persistência no PostgreSQL |
| Spring Data MongoDB | 3.2 | Persistência no MongoDB |
| Clojure | 1.11 | Workers funcionais |
| Leiningen | 2.9+ | Build tool para Clojure |

### Infraestrutura

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Apache Kafka | 3.5 | Message Broker / Event Bus |
| Zookeeper | 7.5 | Coordenação do Kafka |
| PostgreSQL | 16 | Event Store (Write Model) |
| MongoDB | 6.0 | Read Model (Projeções) |
| Docker Compose | 2.0+ | Orquestração de containers |

### Frontend

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| React | 18 | UI Framework |
| Vite | 5 | Build tool |
| TanStack Query | 5 | Server state management |
| Framer Motion | 10 | Animações |
| Tailwind CSS | 3.4 | Styling |

---

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (com Docker Compose)
- [Java JDK 17+](https://adoptium.net/)
- [Leiningen](https://leiningen.org/) (para Clojure)
- [Node.js 18+](https://nodejs.org/)

### Verificar instalações

```bash
docker --version        # Docker version 24+
java -version           # openjdk 17+
lein --version          # Leiningen 2.9+
node --version          # v18+
```

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/duality-store.git
cd duality-store/event-driven-mall
```

### 2. Suba a infraestrutura

```bash
docker compose up -d
```

Aguarde todos os containers iniciarem (~30 segundos). Verifique:
Aguarde todos os containers iniciarem (~30 segundos). Verifique:

```bash
docker compose ps
```

### Serviços disponíveis

| Serviço | URL/Porta |
|---------|-----------|
| Kafka UI | http://localhost:8090 |
| PostgreSQL | localhost:5433 |
| MongoDB | localhost:27018 |
| Kafka Broker | localhost:9092 |

---

## 💻 Como Usar

### ⚡ Quick Start (Windows)

```powershell
# Inicia tudo com 1 clique
.\run-all.cmd

# Para tudo
.\stop-all.cmd
```

### 🐧 Execução Manual (Passo a Passo)

#### Terminal 1 - Command API (porta 8080)
docker compose ps
```

### Serviços disponíveis

| Serviço | URL/Porta |
|---------|-----------|
| Kafka UI | http://localhost:8090 |
| PostgreSQL | localhost:5433 |
| MongoDB | localhost:27018 |
| Kafka Broker | localhost:9092 |

---

## 💻 Como Usar

### ⚡ Quick Start (Windows)

```powershell
# Inicia tudo com 1 clique
.\run-all.cmd

# Para tudo
.\stop-all.cmd
```

### 🐧 Execução Manual (Passo a Passo)

#### Terminal 1 - Command API (porta 8080)
```bash
cd services/command-api
./mvnw spring-boot:run
cd services/command-api
./mvnw spring-boot:run
```

#### Terminal 2 - Inventory Worker (porta 8082)
#### Terminal 2 - Inventory Worker (porta 8082)
```bash
cd services/inventory-worker
./mvnw spring-boot:run
```

#### Terminal 3 - Query API (porta 8081)
```bash
cd services/query-api
./mvnw spring-boot:run
cd services/inventory-worker
./mvnw spring-boot:run
```

#### Terminal 3 - Query API (porta 8081)
```bash
cd services/query-api
./mvnw spring-boot:run
```

#### Terminal 4 - Payment Worker
#### Terminal 4 - Payment Worker
```bash
cd services/payment-worker
lein trampoline run
cd services/payment-worker
lein trampoline run
```

#### Terminal 5 - Projector Worker
#### Terminal 5 - Projector Worker
```bash
cd services/consulta-worker
lein trampoline run
cd services/consulta-worker
lein trampoline run
```

#### Terminal 6 - Frontend (porta 5173)
#### Terminal 6 - Frontend (porta 5173)
```bash
cd services/frontend
npm install
npm run dev
```

### 🪟 Nota para Windows

Se tiver problemas com caminhos contendo acentos no Leiningen:

```powershell
# Crie um drive virtual
subst M: "C:\Caminho\Do\Projeto"
cd M:\event-driven-mall\services\payment-worker

# Configure TEMP sem acentos
$env:TEMP="C:\temp"
$env:TMP="C:\temp"

# Execute
lein deps
lein trampoline run
npm install
npm run dev
```

### 🪟 Nota para Windows

Se tiver problemas com caminhos contendo acentos no Leiningen:

```powershell
# Crie um drive virtual
subst M: "C:\Caminho\Do\Projeto"
cd M:\event-driven-mall\services\payment-worker

# Configure TEMP sem acentos
$env:TEMP="C:\temp"
$env:TMP="C:\temp"

# Execute
lein deps
lein trampoline run
```

---

## 📡 API Reference

### Command API (Porta 8080) - Escrita

#### Criar Pedido
## 📡 API Reference

### Command API (Porta 8080) - Escrita

#### Criar Pedido
```http
POST /orders
Content-Type: application/json

{
  "userId": "user_123",
  "totalAmount": 299.90,
  "items": ["Teclado Mecânico", "Mouse Gamer"]
}
```

**Resposta:** `202 Accepted`
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PROCESSING"
}
```

#### Criar Produto
```http
POST /products
Content-Type: application/json

{
  "name": "Monitor Ultrawide",
  "type": "Eletrônico",
  "price": 2499.90
  "totalAmount": 299.90,
  "items": ["Teclado Mecânico", "Mouse Gamer"]
}
```

**Resposta:** `202 Accepted`
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PROCESSING"
}
```

#### Criar Produto
```http
POST /products
Content-Type: application/json

{
  "name": "Monitor Ultrawide",
  "type": "Eletrônico",
  "price": 2499.90
}
```

#### Consultar Saldo
#### Consultar Saldo
```http
GET /wallets/{userId}
```

### Query API (Porta 8081) - Leitura

| Endpoint | Descrição |
|----------|-----------|
| `GET /orders` | Lista todos os pedidos |
| `GET /orders/{id}` | Busca pedido por ID |
| `GET /orders/user/{userId}` | Pedidos de um usuário |
| `GET /orders/status/{status}` | Pedidos por status |
| `GET /products` | Lista todos os produtos |

---

## 🔄 Fluxo de Eventos

```
┌─────────────────┐
│ OrderCreated    │ ──► Tópico: orders
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ StockReserved   │ ──► Tópico: stock-reserved
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ PaymentSuccess  │ ──► Tópico: payment-success
│ PaymentFailed   │ ──► Tópico: payment-failed
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ OrderCompleted  │ ──► Projetado no MongoDB
└─────────────────┘
```

### Tópicos Kafka

| Tópico | Produtor | Consumidor |
|--------|----------|------------|
| `orders` | Command API | Inventory Worker, Projector |
| `stock-reserved` | Inventory Worker | Payment Worker, Projector |
| `payment-success` | Payment Worker | Projector |
| `payment-failed` | Payment Worker | Projector |
| `products` | Command API | Projector |

---

## 📁 Estrutura do Projeto

```
duality-store/
├── 📄 README.md                    # Este arquivo
├── 🖼️ arquitetura_projeto.jpg      # Diagrama visual
│
└── 📁 event-driven-mall/
    ├── 🐳 docker-compose.yml       # Infraestrutura
    ├── ⚡ run-all.cmd              # Script Windows (start)
    ├── 🛑 stop-all.cmd             # Script Windows (stop)
    │
    └── 📁 services/
        │
        ├── 📁 command-api/         # Java Spring Boot
        │   ├── src/main/java/
        │   │   └── com/mall/command_api/
        │   │       ├── controller/
        │   │       ├── service/
        │   │       ├── entity/
        │   │       ├── repository/
        │   │       └── producer/
        │   └── pom.xml
        │
        ├── 📁 query-api/           # Java Spring Boot
        │   ├── src/main/java/
        │   │   └── com/mall/query_api/
        │   │       ├── controller/
        │   │       ├── document/
        │   │       └── repository/
        │   └── pom.xml
        │
        ├── 📁 inventory-worker/    # Java Spring Boot
        │   ├── src/main/java/
        │   │   └── com/service/
        │   │       └── InventoryListener.java
        │   └── pom.xml
        │
        ├── 📁 payment-worker/      # Clojure
        │   ├── src/payment_worker/
        │   │   ├── core.clj
        │   │   └── db.clj
        │   └── project.clj
        │
        ├── 📁 consulta-worker/     # Clojure (Projector)
        │   ├── src/consulta_worker/
        │   │   ├── core.clj
        │   │   ├── kafka/consumer.clj
        │   │   ├── database/
        │   │   ├── handlers/
        │   │   └── projections/
        │   └── project.clj
        │
        └── 📁 frontend/            # React + Vite
            ├── src/
            │   ├── App.jsx         # Architecture Debugger
            │   └── main.jsx
            ├── package.json
            └── vite.config.js
GET /wallets/{userId}
```

### Query API (Porta 8081) - Leitura

| Endpoint | Descrição |
|----------|-----------|
| `GET /orders` | Lista todos os pedidos |
| `GET /orders/{id}` | Busca pedido por ID |
| `GET /orders/user/{userId}` | Pedidos de um usuário |
| `GET /orders/status/{status}` | Pedidos por status |
| `GET /products` | Lista todos os produtos |

---

## 🔄 Fluxo de Eventos

```
┌─────────────────┐
│ OrderCreated    │ ──► Tópico: orders
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ StockReserved   │ ──► Tópico: stock-reserved
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ PaymentSuccess  │ ──► Tópico: payment-success
│ PaymentFailed   │ ──► Tópico: payment-failed
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ OrderCompleted  │ ──► Projetado no MongoDB
└─────────────────┘
```

### Tópicos Kafka

| Tópico | Produtor | Consumidor |
|--------|----------|------------|
| `orders` | Command API | Inventory Worker, Projector |
| `stock-reserved` | Inventory Worker | Payment Worker, Projector |
| `payment-success` | Payment Worker | Projector |
| `payment-failed` | Payment Worker | Projector |
| `products` | Command API | Projector |

---

## 📁 Estrutura do Projeto

```
duality-store/
├── 📄 README.md                    # Este arquivo
├── 🖼️ arquitetura_projeto.jpg      # Diagrama visual
│
└── 📁 event-driven-mall/
    ├── 🐳 docker-compose.yml       # Infraestrutura
    ├── ⚡ run-all.cmd              # Script Windows (start)
    ├── 🛑 stop-all.cmd             # Script Windows (stop)
    │
    └── 📁 services/
        │
        ├── 📁 command-api/         # Java Spring Boot
        │   ├── src/main/java/
        │   │   └── com/mall/command_api/
        │   │       ├── controller/
        │   │       ├── service/
        │   │       ├── entity/
        │   │       ├── repository/
        │   │       └── producer/
        │   └── pom.xml
        │
        ├── 📁 query-api/           # Java Spring Boot
        │   ├── src/main/java/
        │   │   └── com/mall/query_api/
        │   │       ├── controller/
        │   │       ├── document/
        │   │       └── repository/
        │   └── pom.xml
        │
        ├── 📁 inventory-worker/    # Java Spring Boot
        │   ├── src/main/java/
        │   │   └── com/service/
        │   │       └── InventoryListener.java
        │   └── pom.xml
        │
        ├── 📁 payment-worker/      # Clojure
        │   ├── src/payment_worker/
        │   │   ├── core.clj
        │   │   └── db.clj
        │   └── project.clj
        │
        ├── 📁 consulta-worker/     # Clojure (Projector)
        │   ├── src/consulta_worker/
        │   │   ├── core.clj
        │   │   ├── kafka/consumer.clj
        │   │   ├── database/
        │   │   ├── handlers/
        │   │   └── projections/
        │   └── project.clj
        │
        └── 📁 frontend/            # React + Vite
            ├── src/
            │   ├── App.jsx         # Architecture Debugger
            │   └── main.jsx
            ├── package.json
            └── vite.config.js
```

---

## 🔮 Roadmap

- [x] Implementação base de CQRS
- [x] Event Sourcing com PostgreSQL
- [x] Workers em Clojure
- [x] Projeções no MongoDB
- [x] Frontend com debugger visual
- [x] Sistema de Wallet/Saldo
- [ ] **Saga Pattern** - Orquestração de falhas distribuídas
- [ ] **WebSocket** - Atualizações em tempo real
- [ ] **Kubernetes** - Deploy containerizado
- [ ] **Testes de Carga** - Comparativo Escrita vs Leitura
- [ ] **Dead Letter Queue** - Tratamento de eventos falhos
- [ ] **Métricas** - Prometheus + Grafana

---

## 🧪 Testando o Sistema

### 1. Criar um produto (Admin)
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado RGB", "type": "Periférico", "price": 299.90}'
```

### 2. Verificar projeção (Query API)
```bash
curl http://localhost:8081/products
```

### 3. Fazer uma compra
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": "comprador-1", "totalAmount": 299.90, "items": ["Teclado RGB"]}'
```

### 4. Acompanhar no Kafka UI
Acesse http://localhost:8090 e veja as mensagens fluindo pelos tópicos.

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:

1. Fazer fork do projeto
2. Criar uma branch (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Add: nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abrir um Pull Request

---

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
- [x] Implementação base de CQRS
- [x] Event Sourcing com PostgreSQL
- [x] Workers em Clojure
- [x] Projeções no MongoDB
- [x] Frontend com debugger visual
- [x] Sistema de Wallet/Saldo
- [ ] **Saga Pattern** - Orquestração de falhas distribuídas
- [ ] **WebSocket** - Atualizações em tempo real
- [ ] **Kubernetes** - Deploy containerizado
- [ ] **Testes de Carga** - Comparativo Escrita vs Leitura
- [ ] **Dead Letter Queue** - Tratamento de eventos falhos
- [ ] **Métricas** - Prometheus + Grafana

---

## 🧪 Testando o Sistema

### 1. Criar um produto (Admin)
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado RGB", "type": "Periférico", "price": 299.90}'
```

### 2. Verificar projeção (Query API)
```bash
curl http://localhost:8081/products
```

### 3. Fazer uma compra
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": "comprador-1", "totalAmount": 299.90, "items": ["Teclado RGB"]}'
```

### 4. Acompanhar no Kafka UI
Acesse http://localhost:8090 e veja as mensagens fluindo pelos tópicos.

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:

1. Fazer fork do projeto
2. Criar uma branch (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Add: nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abrir um Pull Request

---

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👨‍💻 Autor

<p align="center">
  <strong>João Pedro Hornos</strong>
</p>

<p align="center">
  <a href="https://github.com/raidenario">
    <img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"/>
  </a>
  <a href="https://linkedin.com/in/joão-hornos">
    <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn"/>
  </a>
</p>

---

<p align="center">
  <sub>Feito com ☕ e muito ☯️</sub>
</p>
