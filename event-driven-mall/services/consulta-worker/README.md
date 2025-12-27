# 📊 Projection Worker (Consulta Worker)

Worker CQRS responsável por consumir eventos do Kafka e projetar no MongoDB (Read Model).

## 🎯 Responsabilidades

- Escutar eventos dos tópicos Kafka: `orders`, `stock-reserved`, `payment-success`
- Projetar eventos no MongoDB para otimizar consultas
- Manter o Read Model sempre atualizado
- Garantir consistência eventual

## 🏗️ Arquitetura

```
Kafka Topics → Consumer → Event Handler → Projections → MongoDB
```

## 📁 Estrutura de Pastas

```
consulta-worker/
├── project.clj              # Configuração Leiningen
├── README.md
├── resources/
│   └── logback.xml          # Configuração de logs
├── test/
│   └── consulta_worker/
│       └── core_test.clj
└── src/
    └── consulta_worker/
        ├── core.clj                    # Orquestração principal
        ├── config.clj                  # Configurações (Kafka, MongoDB)
        │
        ├── kafka/                      # 📨 Camada de Mensageria
        │   └── consumer.clj            # Lógica do KafkaConsumer
        │
        ├── database/                   # 💾 Camada de Persistência
        │   ├── connection.clj          # Conexão com MongoDB
        │   └── repository.clj          # Operações CRUD genéricas
        │
        ├── projections/                # 🔄 Lógica de Projeções
        │   └── order_projection.clj    # Projeções de pedidos
        │
        ├── handlers/                   # 🎯 Roteamento de Eventos
        │   └── event_handler.clj       # Multi-method para eventos
        │
        └── utils/                      # 🛠️ Utilitários
            ├── time.clj                # Funções de data/hora
            └── json.clj                # Helpers de JSON
```

## 🔄 Fluxo de Eventos

### 1. OrderCreated (Tópico: `orders`)
```clojure
{:orderId "123"
 :userId "user_456"
 :totalAmount 199.90
 :items ["Teclado", "Mouse"]}
```
**Ação**: Cria documento inicial no MongoDB com status `CREATED`

### 2. StockReserved (Tópico: `stock-reserved`)
```clojure
{:orderId "123"
 :status "RESERVED"}
```
**Ação**: Atualiza status para `STOCK_RESERVED`

### 3. PaymentSuccess (Tópico: `payment-success`)
```clojure
{:orderId "123"
 :paymentId "pay_789"
 :status "PAYMENT_SUCCESS"}
```
**Ação**: Atualiza status para `COMPLETED`

## 🚀 Como Rodar

### Pré-requisitos
- Leiningen instalado
- Kafka rodando (localhost:9092)
- MongoDB rodando (localhost:27018)

### Desenvolvimento
```bash
# Instalar dependências
lein deps

# Rodar worker
lein run

# Ou com trampoline (recomendado no Windows)
lein trampoline run
# ou
lein run!
```

### Produção
```bash
# Gerar uberjar
lein uberjar

# Rodar
java -jar target/uberjar/projection-worker-0.1.0-SNAPSHOT-standalone.jar
```

## ⚙️ Configurações

### Kafka (`config.clj`)
```clojure
{:bootstrap-servers "localhost:9092"
 :consumer-group    "projector-group"
 :topics            ["orders" "stock-reserved" "payment-success"]
 :poll-timeout-ms   1000}
```

### MongoDB (`config.clj`)
```clojure
{:host       "localhost"
 :port       27018
 :db         "event_store_db"
 :username   "admin"
 :password   "password"
 :collection "orders"}
```

## 📊 Estrutura do Documento MongoDB

```javascript
{
  "_id": "order_123",
  "orderId": "order_123",
  "userId": "user_456",
  "amount": 199.90,
  "items": ["Teclado Mecânico", "Mouse Gamer"],
  "status": "COMPLETED",
  "createdAt": "2025-12-26T10:00:00Z",
  "updatedAt": "2025-12-26T10:05:00Z",
  "stockReservedAt": "2025-12-26T10:02:00Z",
  "completedAt": "2025-12-26T10:05:00Z",
  "paymentId": "pay_789",
  "history": [
    {
      "status": "CREATED",
      "timestamp": "2025-12-26T10:00:00Z",
      "description": "Pedido criado"
    },
    {
      "status": "STOCK_RESERVED",
      "timestamp": "2025-12-26T10:02:00Z",
      "description": "Estoque reservado com sucesso"
    },
    {
      "status": "COMPLETED",
      "timestamp": "2025-12-26T10:05:00Z",
      "paymentId": "pay_789",
      "description": "Pagamento processado com sucesso"
    }
  ]
}
```

## 🧪 Testes

```bash
# Rodar todos os testes
lein test

# Rodar com auto-reload
lein test-refresh
```

## 📝 Logs

Os logs são configurados via `resources/logback.xml`:

- **INFO**: Eventos principais (pedido criado, estoque reservado, etc.)
- **DEBUG**: Detalhes de processamento
- **ERROR**: Erros e exceções

Exemplo de log:
```
2025-12-26 10:00:00 INFO  [main] 📊 PROJECTION WORKER - Event-Driven Mall
2025-12-26 10:00:01 INFO  [main] 🔌 Conectando ao MongoDB...
2025-12-26 10:00:02 INFO  [main] ✅ MongoDB conectado com sucesso!
2025-12-26 10:00:03 INFO  [main] 🔌 Criando Kafka Consumer...
2025-12-26 10:00:04 INFO  [main] ✅ Kafka Consumer criado e inscrito nos tópicos
2025-12-26 10:00:05 INFO  [main] 🔄 Iniciando loop de consumo de mensagens...
2025-12-26 10:00:10 INFO  [main] 📝 [Projector] Pedido criado: order_123
```

## 🛠️ Tecnologias

- **Clojure 1.11**: Linguagem funcional
- **Kafka Clients 3.5.1**: Cliente Java oficial do Kafka
- **Monger 3.6.0**: Driver MongoDB para Clojure
- **Cheshire 5.11.0**: Parser/Generator JSON
- **Logback 1.2.13**: Framework de logging

## 🔧 Troubleshooting

### Erro: "Could not locate consulta_worker/core.clj"
**Causa**: Caminho com acentos no Windows

**Solução**:
```powershell
subst M: "C:\Users\João Pedro\Documents\PROJETOS LEGÍTIMOS"
cd M:\event-driven-mall\services\consulta-worker
$env:TEMP="C:\temp"; $env:TMP="C:\temp"
lein deps
lein run!
```

### Erro: "Connection refused" (MongoDB)
**Causa**: MongoDB não está rodando

**Solução**:
```bash
cd ../../
docker compose up -d mongodb
```

### Erro: "Connection refused" (Kafka)
**Causa**: Kafka não está rodando

**Solução**:
```bash
cd ../../
docker compose up -d kafka zookeeper
```

## 📚 Referências

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [MongoDB Manual](https://docs.mongodb.com/manual/)
- [Monger Documentation](http://clojuremongodb.info/)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)

## 📄 Licença

EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0
