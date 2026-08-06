# BankHub - Sistema Bancário Microservices

Sistema bancário completo implementado com arquitetura de microservices, incluindo contas, investimentos, transações PIX, onboarding e assistente de IA.

---

## 🏗️ Arquitetura

### Serviços (Porta)

- **API Gateway** (8080) - Roteamento, rate limiting, CORS
- **Account Service** (8081) - Gestão de contas, cartões, KYC
- **Onboarding Service** (8082) - Análise de risco com Camunda
- **Hub IA Service** (8084) - Assistente AI com LangChain4j
- **Transaction Service** (8085) - PIX, ledger, extratos
- **Investment Service** (8086) - Home broker, portfólios
- **Notification Service** - Emails/SMS via Kafka (sem porta REST)

### Infraestrutura

- **MongoDB** (27017) - Banco de dados
- **Redis** (6379) - Cache e rate limiting
- **Kafka** (9092) - Mensageria assíncrona
- **Keycloak** (9000) - Autenticação OAuth2/JWT ✨ **NOVO**
- **Jaeger** (16686) - Distributed tracing UI ✨ **NOVO**
- **OpenTelemetry Collector** (4317/4318) - Coleta de traces ✨ **NOVO**
- **MailHog** (8025) - Email local (dev)
- **Postfix** (1587) - SMTP production ✨ **NOVO**
- **Zeebe** (26500) - Workflow engine (Camunda)
- **LiteLLM** (4000) - Proxy para LLMs

---

## 🚀 Como Iniciar

### 1. Pré-requisitos

```bash
# Verificar instalação
docker --version
docker-compose --version
java -version  # Java 21
```

### 2. Subir Infraestrutura

```bash
# Subir todos os componentes de infraestrutura
docker-compose -f docker-compose-infra.yml up -d

# Verificar se tudo está rodando
docker-compose -f docker-compose-infra.yml ps
```

**Aguardar ~30 segundos** para que Keycloak, Kafka e MongoDB inicializem.

### 3. Subir Aplicações

```bash
# Build dos serviços
./gradlew clean build -x test

# Subir serviços via Docker
docker-compose -f docker-compose-apps.yml up -d

# OU executar localmente para desenvolvimento
./gradlew :account-service:bootRun
./gradlew :investment-service:bootRun
# ... outros serviços conforme necessário
```

### 4. Verificar Status

```bash
# Health checks
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Account
curl http://localhost:8086/actuator/health  # Investment
```

---

## 🔐 Autenticação com Keycloak

### Acessar Console Admin

- **URL:** http://localhost:9000
- **Usuário:** `admin`
- **Senha:** `admin`
- **Realm:** `bankhub`

### Usuários de Teste

O realm vem pré-configurado com 3 usuários:

| Email | Senha | Role | Customer ID |
|-------|-------|------|-------------|
| `customer1@bankhub.com` | `senha123` | customer | `customer-001` |
| `customer2@bankhub.com` | `senha123` | customer | `customer-002` |
| `admin@bankhub.com` | `admin123` | admin | - |

### Obter Token JWT

#### Via Postman/Insomnia:

```
POST http://localhost:9000/realms/bankhub/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

Body (form-data):
  grant_type: password
  client_id: bankhub-services
  client_secret: bankhub-secret-2024
  username: customer1@bankhub.com
  password: senha123
```

#### Via cURL:

```bash
# Obter token do customer1
curl -X POST 'http://localhost:9000/realms/bankhub/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=bankhub-services' \
  -d 'client_secret=bankhub-secret-2024' \
  -d 'username=customer1@bankhub.com' \
  -d 'password=senha123'

# Extrair apenas o access_token (requer jq)
TOKEN=$(curl -s -X POST 'http://localhost:9000/realms/bankhub/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=bankhub-services' \
  -d 'client_secret=bankhub-secret-2024' \
  -d 'username=customer1@bankhub.com' \
  -d 'password=senha123' | jq -r .access_token)

echo $TOKEN
```

**O token retornado inclui:**
- `customerId` claim (usado em X-User-Id header)
- Roles (customer/admin)
- Validade: 30 minutos

---

## 🧪 Testando as APIs

### Via API Gateway (Recomendado)

Todas as chamadas devem passar pelo Gateway na porta **8080**:

```bash
# Usar o token obtido anteriormente
TOKEN="seu_token_aqui"

# 1. Criar conta
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "12345678901",
    "name": "João Silva",
    "email": "joao@example.com",
    "birthdate": "1990-01-01"
  }'

# 2. Consultar conta
curl http://localhost:8080/api/v1/accounts/{accountId} \
  -H "Authorization: Bearer $TOKEN"

# 3. Fazer depósito
curl -X POST http://localhost:8080/api/v1/accounts/{accountId}/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "transactionPin": "1234"
  }'

# 4. Consultar portfólio de investimentos
curl http://localhost:8080/api/v1/investments/portfolio/customer-001 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: customer-001"
```

### Swagger UI

Cada serviço expõe documentação OpenAPI:

- **Gateway:** http://localhost:8080/swagger-ui.html
- **Account:** http://localhost:8081/swagger-ui.html
- **Investment:** http://localhost:8086/swagger-ui.html
- **Transaction:** http://localhost:8085/swagger-ui.html

---

## 🔒 Teste de Segurança (CRÍTICO)

### ✅ Fix Aplicado: Autorização de Portfólio

**Problema corrigido:** Investment Service agora valida que usuários só podem acessar seu próprio portfólio.

#### Testar Acesso Autorizado:

```bash
# customer-001 acessando próprio portfólio (deve retornar 200)
curl http://localhost:8080/api/v1/investments/portfolio/customer-001 \
  -H "Authorization: Bearer $TOKEN_CUSTOMER1" \
  -H "X-User-Id: customer-001"
```

#### Testar Acesso NÃO Autorizado:

```bash
# customer-001 tentando acessar portfólio do customer-002 (deve retornar 403)
curl http://localhost:8080/api/v1/investments/portfolio/customer-002 \
  -H "Authorization: Bearer $TOKEN_CUSTOMER1" \
  -H "X-User-Id: customer-001"

# Resposta esperada:
# HTTP 403 Forbidden
# "Você não tem permissão para acessar a carteira de outro cliente."
```

---

## 📊 Monitoramento e Observabilidade

### Jaeger UI - Distributed Tracing

**URL:** http://localhost:16686

Visualize traces de requisições atravessando múltiplos serviços:
1. Acesse Jaeger UI
2. Selecione serviço (ex: `account-service`)
3. Clique em "Find Traces"
4. Explore spans e latências

### Métricas OpenTelemetry

**Prometheus Exporter:** http://localhost:8888/metrics

### MailHog - Email Testing (Dev)

**URL:** http://localhost:8025

Visualize emails enviados em desenvolvimento (activation links, notifications).

---

## 🛠️ Desenvolvimento

### Estrutura Hexagonal (Ports & Adapters)

Todos os serviços seguem o padrão:

```
com.bankhub.{service}
├── application/
│   ├── port/in/       # Use cases (interfaces)
│   ├── port/out/      # Portas externas (interfaces)
│   └── service/       # Implementações use cases
├── domain/            # Entidades e regras de negócio
└── infrastructure/
    ├── web/           # Controllers REST + DTOs
    ├── persistence/   # Adapters MongoDB
    └── messaging/     # Kafka listeners
```

### Executar Testes

```bash
# Todos os testes
./gradlew test

# Serviço específico
./gradlew :account-service:test

# Testes de integração (usa Testcontainers)
./gradlew :account-service:test --tests "*IntegrationTest"
```

**Nota:** Testcontainers requer Docker rodando.

### Padrões de Código

- **DTOs:** Usar `record` (Java 21+), não `@Data`
- **Validação:** Anotações Jakarta Validation (`@NotNull`, `@NotBlank`)
- **Mapeamento:** MapStruct automático
- **Testes:** JUnit 5 + Mockito + AssertJ + Testcontainers

---

## 🔧 Variáveis de Ambiente

### Keycloak (Obrigatório)

```bash
KEYCLOAK_JWK_URI=http://keycloak:8080/realms/bankhub/protocol/openid-connect/certs
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/bankhub
```

### MongoDB

```bash
SPRING_DATA_MONGODB_URI=mongodb://admin:admin@localhost:27017/bankhub_account?authSource=admin
```

### Kafka

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### OpenTelemetry (Opcional)

```bash
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
```

### Email Production (Postfix)

```bash
SMTP_HOST=postfix
SMTP_PORT=587
SPRING_PROFILES_ACTIVE=prod  # Usar Postfix ao invés de MailHog
```

---

## 📦 Build e Deploy

### Build Local

```bash
./gradlew clean build
```

### Build Docker Images

```bash
# Individual
docker build -t bankhub/account-service:latest ./account-service

# Todos (via docker-compose)
docker-compose -f docker-compose-apps.yml build
```

### Docker Compose Profiles

```bash
# Apenas infraestrutura (dev)
docker-compose -f docker-compose-infra.yml up -d

# Com Postfix (production)
docker-compose -f docker-compose-infra.yml --profile production up -d
```

---

## 🐛 Troubleshooting

### Keycloak não inicia

```bash
# Verificar logs
docker logs bank-keycloak

# Reiniciar
docker-compose -f docker-compose-infra.yml restart keycloak
```

### Token JWT inválido (401)

- Verificar se realm é `bankhub` (não `master`)
- Token expira em 30 minutos
- Verificar se `X-User-Id` header está presente

### MongoDB connection refused

```bash
# Verificar se MongoDB está rodando
docker ps | grep mongo

# Verificar credenciais
# User: admin / Password: admin
```

### Kafka consumer não processa mensagens

```bash
# Verificar tópicos
docker exec -it bank-kafka kafka-topics --list --bootstrap-server localhost:9092

# Ver mensagens
docker exec -it bank-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic bankhub.account.events \
  --from-beginning
```

---

## 📚 Recursos Adicionais

- **Keycloak Docs:** https://www.keycloak.org/documentation
- **OpenTelemetry:** https://opentelemetry.io/docs/
- **Testcontainers:** https://www.testcontainers.org/
- **Spring Cloud Gateway:** https://spring.io/projects/spring-cloud-gateway
- **Camunda 8:** https://docs.camunda.io/

---

## ✨ Novidades Recentes

### Security Fix (CRÍTICO) ✅
- **Fix aplicado:** Investment Service agora valida autorização em `GET /portfolio/{customerId}`
- Usuários não podem mais acessar portfólios de outros clientes
- Documentado em OpenAPI com resposta 403

### Keycloak Integration ✅
- Realm `bankhub` pré-configurado
- 3 usuários de teste prontos
- JWT com claim `customerId`
- Validação automática em todos os serviços

### Observability Stack ✅
- OpenTelemetry Collector + Jaeger
- Distributed tracing across services
- Prometheus metrics endpoint

### Email Configuration ✅
- MailHog (desenvolvimento)
- Postfix (produção) - Docker local
- Profiles Spring para alternar

### Test Infrastructure ✅
- Testcontainers configurado (MongoDB, Kafka, Redis)
- Base classes para unit e integration tests
- Pronto para adicionar cobertura

---

## 🤝 Contribuindo

1. Criar branch: `git checkout -b feature/minha-feature`
2. Seguir padrões de código (hexagonal, DTOs como records)
3. Adicionar testes (unit + integration)
4. Commit: `git commit -m "feat: minha feature"`
5. Push: `git push origin feature/minha-feature`

---

## 📄 Licença

Projeto acadêmico / aprendizado - uso livre.

---

**Versão:** 1.0.0-SNAPSHOT  
**Java:** 21  
**Spring Boot:** 3.2.2  
**Última atualização:** 2026-08-06
