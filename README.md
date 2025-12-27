# ☯️ Duality Store

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)
![Clojure](https://img.shields.io/badge/Clojure-1.11-blue?style=for-the-badge\&logo=clojure\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge\&logo=spring-boot\&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-3.5-231F20?style=for-the-badge\&logo=apache-kafka\&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge\&logo=docker\&logoColor=white)
![Postgres](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge\&logo=postgresql\&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-6.0-47A248?style=for-the-badge\&logo=mongodb\&logoColor=white)

> **Uma simulação de e-commerce distribuída explorando a dualidade entre consistência transacional e performance de leitura através de CQRS, Event Sourcing e Arquitetura Poliglota.**

---

## 📐 A Arquitetura da Dualidade

O **Duality Store** foi projetado para demonstrar como paradigmas diferentes podem coexistir para resolver problemas específicos. A arquitetura se baseia em dois pilares principais de separação:

1. **Dualidade de Dados (CQRS):** Separação estrita entre o modelo de escrita (Command/Postgres) e o modelo de leitura (Query/MongoDB).
2. **Dualidade de Linguagem:** Uso da robustez do **Java** para regras de negócio e APIs, combinado com a expressividade funcional do **Clojure** para processamento de streams e projeções.

### Diagrama do Fluxo

![Arquitetura do Projeto](arquitetura_projeto_pedidos.jpg)

### Componentes e Responsabilidades

* **Command API (Java):** O "lado esquerdo" da dualidade. Recebe intenções de compra, valida regras de domínio e persiste a verdade absoluta no PostgreSQL.
* **Event Bus (Kafka):** A espinha dorsal que desacopla os sistemas, permitindo que a escrita e a leitura evoluam em ritmos diferentes.
* **Workers Especializados:**

  * **Inventory Worker (Java):** Consistência forte para controle de estoque.
  * **Payment Worker (Clojure):** Integração funcional e assíncrona.
  * **Projector Worker (Clojure):** O "tradutor" da dualidade. Escuta eventos de negócio e materializa visões otimizadas no MongoDB.
* **Query API (Java):** O "lado direito" da dualidade. Entrega dados prontos para consumo imediato pelo Frontend, sem processamento pesado.

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos

* Docker & Docker Compose
* Java JDK 17+
* Leiningen (para Clojure)
* Node.js (para Frontend)

### ⚡ Quick Start (Windows)

Para facilitar a execução em ambiente Windows e evitar conflitos de variáveis de ambiente:

* 🟢 **Start:** Execute o script `run-all.cmd` (sobe a infraestrutura e abre terminais para cada serviço).
* 🔴 **Stop:** Execute o script `stop-all.cmd`.

### 👣 Execução Manual (Passo a Passo)

#### 1. Infraestrutura (Docker)

```bash
docker compose up -d
```

* Kafka UI: [http://localhost:8090](http://localhost:8090)
* Postgres: Porta 5433
* MongoDB: Porta 27018

#### 2. Serviços Java (Spring Boot)

Em terminais separados:

```bash
# Terminal 1: Command API (Porta 8080)
cd services/command-api && ./mvnw spring-boot:run
```

```bash
# Terminal 2: Inventory Worker (Porta 8082)
cd services/inventory-worker && ./mvnw spring-boot:run
```

```bash
# Terminal 3: Query API (Porta 8081)
cd services/query-api && ./mvnw spring-boot:run
```

#### 3. Workers Clojure

Em novos terminais:

```bash
# Terminal 4: Payment Worker
cd services/payment-worker && lein run
```

```bash
# Terminal 5: Projector Worker
cd services/consulta-worker && lein run
```

> **Dica:** Se estiver no Windows e tiver problemas com caminhos longos ou acentos no Leiningen, use o comando:
>
> ```bash
> subst M: "C:\\Caminho\\Do\\Projeto"
> ```
>
> E rode o projeto a partir do drive virtual `M:`.

#### 4. Frontend (React)

```bash
# Terminal 6: Frontend (Porta 3000)
cd services/frontend
npm install && npm run dev
```

---

## 🛠️ Tech Stack

| Componente       | Tecnologia                 | Papel na Dualidade                                                    |
| ---------------- | -------------------------- | --------------------------------------------------------------------- |
| Command API      | Java 17, Spring Boot 3     | Escrita: Segurança, Tipagem Forte, Transações ACID                    |
| Inventory Worker | Java 17, Spring Kafka      | Processamento: Regras de Negócio Críticas                             |
| Payment Worker   | Clojure 1.11               | Processamento: Imutabilidade, Tratamento de Dados Funcional           |
| Projector Worker | Clojure 1.11, Monger       | Projeção: Transformação de Evento → Documento (ETL)                   |
| Query API        | Java 17, Spring Data Mongo | Leitura: Alta Disponibilidade, Baixa Latência                         |
| Storage          | PostgreSQL & MongoDB       | Persistência: Relacional (Normalizado) vs Documental (Desnormalizado) |

---

## 📡 Endpoints Principais

### Command API (Escrita — Porta 8080)

```http
POST /orders
Content-Type: application/json

{
  "userId": "user_123",
  "totalAmount": 1500.00,
  "items": ["Monitor Ultrawide", "Suporte Articulado"]
}
```

### Query API (Leitura — Porta 8081)

```http
GET /orders/{id}        # Status em tempo real (atualizado via Eventual Consistency)
GET /orders/user/{id}   # Histórico completo do usuário
```

---

## 🔮 Roadmap

* [ ] Implementação de **Saga Pattern** (orquestração de falhas distribuídas).
* [ ] Adição de **WebSocket** no Frontend para atualização de status em tempo real.
* [ ] Containerização completa para deploy em **Kubernetes**.
* [ ] Testes de carga comparando a latência de Escrita vs. Leitura.

---

**Autor:** Joao Pedro Hornos
