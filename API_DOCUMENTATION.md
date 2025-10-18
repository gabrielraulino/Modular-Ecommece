# 🛍️ E-commerce Modular Monolith - Documentação Completa

## 📋 Índice

- [Overview](#overview)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Módulos](#módulos)
- [Eventos de Domínio](#eventos-de-domínio)
- [API Endpoints](#api-endpoints)
- [Funcionalidades](#funcionalidades)
- [Regras de Negócio](#regras-de-negócio)
- [Como Executar](#como-executar)
- [Exemplos de Uso](#exemplos-de-uso)

---

## Overview

Sistema de e-commerce desenvolvido seguindo a arquitetura **Modular Monolith** usando **Spring Modulith** e **Event-Driven Architecture**. A aplicação implementa um fluxo completo de compras online com gerenciamento de produtos, carrinhos, pedidos e usuários, utilizando eventos para comunicação assíncrona entre módulos.

### Características Principais

- ✅ **Arquitetura Modular**: Módulos independentes e desacoplados
- ✅ **Event-Driven**: Comunicação via eventos de domínio
- ✅ **Processamento Assíncrono**: Operações não-bloqueantes
- ✅ **Gerenciamento de Estoque**: Atualização automática via eventos
- ✅ **Transactional Outbox**: Garantia de entrega de eventos
- ✅ **RESTful API**: Endpoints bem definidos
- ✅ **Swagger/OpenAPI**: Documentação interativa
- ✅ **PostgreSQL**: Banco de dados robusto

---

## Arquitetura

### Visão Geral

```
┌─────────────────────────────────────────────────────────────┐
│                    E-commerce Application                   │
│                   (Modular Monolith)                        │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   ┌────▼────┐        ┌─────▼─────┐      ┌─────▼─────┐
   │  User   │        │  Product  │      │   Cart    │
   │ Module  │        │  Module   │      │  Module   │
   └─────────┘        └───────────┘      └─────┬─────┘
                                               │
                                    CheckoutEvent
                                               │
                      ┌────────────────────────┼────────────┐
                      │                        │            │
                ┌─────▼─────┐           ┌──────▼──────┐     │
                │   Order   │           │   Product   │     │
                │  Module   │           │   Module    │     │
                └─────┬─────┘           └─────────────┘     │
                      │                                     │
          OrderCancelledEvent                               │
                      │                                     │
                      └─────────────────────────────────────┘
```

### Padrões Implementados

- **Domain-Driven Design (DDD)**: Bounded contexts bem definidos
- **Event-Driven Architecture**: Comunicação via eventos
- **CQRS**: Separação de comandos e consultas
- **Transactional Outbox Pattern**: Consistência eventual
- **Repository Pattern**: Abstração de acesso a dados
- **DTO Pattern**: Transferência de dados entre camadas

---

## Tecnologias

### Backend
- **Java 21**: Linguagem de programação
- **Spring Boot 3.5.6**: Framework principal
- **Spring Modulith 1.4.1**: Arquitetura modular
- **Spring Data JPA**: Persistência de dados
- **Hibernate**: ORM
- **PostgreSQL**: Banco de dados relacional

### Ferramentas
- **Lombok**: Redução de boilerplate
- **Springdoc OpenAPI**: Documentação Swagger
- **Maven**: Gerenciamento de dependências

---

## Módulos

### 1. User Module (Usuários)

**Responsabilidade:** Gerenciamento de usuários

**Entidades:**
- `User`: Representa um usuário do sistema

**DTOs:**
- `UserDTO`: Dados do usuário para leitura
- `UserCreateDTO`: Dados para criação de usuário

**API Pública:**
- `UserModuleAPI`: Interface para outros módulos consultarem usuários

**Funcionalidades:**
- Criar usuário
- Buscar usuário por ID
- Listar todos os usuários
- Atualizar usuário
- Deletar usuário

---

### 2. Product Module (Produtos)

**Responsabilidade:** Gerenciamento de produtos e estoque

**Entidades:**
- `Product`: Representa um produto com estoque

**DTOs:**
- `ProductDTO`: Dados do produto para leitura
- `CreateProductDTO`: Dados para criação/atualização

**API Pública:**
- `ProductModuleAPI`: Interface para consultar produtos

**Funcionalidades:**
- CRUD completo de produtos
- Atualização de estoque (quantidade relativa)
- Atualização de estoque (quantidade absoluta)
- **Listener de Checkout**: Decrementa estoque automaticamente
- **Listener de Cancelamento**: Incrementa estoque automaticamente

**Event Listeners:**
```java
@ApplicationModuleListener
void onCheckoutEvent(CheckoutEvent event)
// Decrementa estoque quando pedido é criado

@ApplicationModuleListener
void onOrderCancelledEvent(OrderCancelledEvent event)
// Incrementa estoque quando pedido é cancelado
```

---

### 3. Cart Module (Carrinho de Compras)

**Responsabilidade:** Gerenciamento de carrinhos de compras

**Entidades:**
- `Cart`: Carrinho de um usuário
- `CartItem`: Item dentro do carrinho

**DTOs:**
- `CartDTO`: Dados completos do carrinho
- `CartItemDTO`: Dados de um item com informações do produto
- `addCartItemDTO`: Dados para adicionar item

**Funcionalidades:**
- Adicionar produto ao carrinho
- Listar carrinhos
- Buscar carrinho por usuário
- **Checkout**: Finaliza compra e publica evento

**Regras:**
- Um usuário tem um carrinho
- Carrinho pode ter múltiplos itens
- Adicionar mesmo produto incrementa quantidade
- Checkout limpa o carrinho e publica evento

---

### 4. Order Module (Pedidos)

**Responsabilidade:** Gerenciamento de pedidos

**Entidades:**
- `Order`: Representa um pedido
- `OrderItem`: Item dentro do pedido
- `OrderStatus`: Enum de status do pedido

**DTOs:**
- `OrderDTO`: Dados completos do pedido
- `OrderItemDTO`: Dados de um item do pedido

**Funcionalidades:**
- Listar pedidos
- Buscar pedido por ID
- Listar pedidos por usuário
- **Cancelar pedido**: Cancela e publica evento
- **Listener de Checkout**: Cria pedido automaticamente

**Status de Pedido:**
```java
PENDING      // Pedido criado
PROCESSING   // Em processamento
SHIPPED      // Enviado
DELIVERED    // Entregue (não pode cancelar)
CANCELLED    // Cancelado
```

**Event Listeners:**
```java
@ApplicationModuleListener
void onCheckoutEvent(CheckoutEvent event)
// Cria pedido quando checkout é realizado
```

---

### 5. Event Module (Eventos de Domínio)

**Responsabilidade:** Definição de eventos de domínio

**Eventos:**

#### CheckoutEvent
```java
public record CheckoutEvent(
    Long cartId,
    Long userId,
    List<CheckoutItem> items,
    BigDecimal totalAmount,
    String currency,
    LocalDateTime checkoutDate
)
```
**Publicado por:** CartService.checkout()  
**Consumido por:** OrderService (cria pedido), ProductService (decrementa estoque)

#### OrderCancelledEvent
```java
public record OrderCancelledEvent(
    Long orderId,
    Long userId,
    List<CancelledItem> items,
    LocalDateTime cancelledDate
)
```
**Publicado por:** OrderService.cancelOrder()  
**Consumido por:** ProductService (incrementa estoque)

---

## Eventos de Domínio

### Fluxo de Checkout

```
1. Cliente → POST /api/carts/user/{userId}/checkout
                    ↓
2. CartService publica CheckoutEvent
                    ↓
        ┌───────────┴───────────┐
        ↓                       ↓
3a. OrderService         3b. ProductService
    escuta evento            escuta evento
        ↓                       ↓
4a. Cria Order          4b. Decrementa estoque
        ↓                       ↓
5. Cliente ← Carrinho vazio (resposta imediata)
```

### Fluxo de Cancelamento

```
1. Cliente → POST /api/orders/{id}/cancel
                    ↓
2. OrderService atualiza status → CANCELLED
                    ↓
3. OrderService publica OrderCancelledEvent
                    ↓
4. ProductService escuta evento
                    ↓
5. ProductService incrementa estoque
                    ↓
6. Cliente ← Order com status CANCELLED
```

---

## API Endpoints

### 👤 User Endpoints

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| **POST** | `/api/users` | Criar usuário | `UserCreateDTO` |
| **GET** | `/api/users` | Listar todos usuários | - |
| **GET** | `/api/users/{id}` | Buscar usuário por ID | - |
| **PUT** | `/api/users/{id}` | Atualizar usuário | `UserCreateDTO` |
| **DELETE** | `/api/users/{id}` | Deletar usuário | - |

**UserCreateDTO:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string"
}
```

---

### 📦 Product Endpoints

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| **POST** | `/api/products` | Criar produto | `CreateProductDTO` |
| **GET** | `/api/products` | Listar todos produtos | - |
| **GET** | `/api/products/{id}` | Buscar produto por ID | - |
| **PUT** | `/api/products/{id}` | Atualizar produto | `CreateProductDTO` |
| **DELETE** | `/api/products/{id}` | Deletar produto | - |
| **PATCH** | `/api/products/{id}/quantity?qty={int}` | Atualizar quantidade (relativo) | - |
| **PATCH** | `/api/products/{id}/stock?stock={int}` | Atualizar estoque (absoluto) | - |

**CreateProductDTO:**
```json
{
  "name": "string",
  "description": "string",
  "priceAmount": 0.00,
  "priceCurrency": "BRL",
  "stock": 0
}
```

---

### 🛒 Cart Endpoints

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| **POST** | `/api/carts` | Adicionar item ao carrinho | `addCartItemDTO` |
| **GET** | `/api/carts` | Listar todos carrinhos | - |
| **GET** | `/api/carts/user/{userId}` | Buscar carrinho do usuário | - |
| **POST** | `/api/carts/user/{userId}/checkout` | ⭐ **Realizar checkout** | - |

**addCartItemDTO:**
```json
{
  "userId": 1,
  "productId": 1,
  "quantity": 2
}
```

**Resposta CartDTO:**
```json
{
  "id": 1,
  "userId": 1,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Produto X",
      "priceAmount": 100.00,
      "priceCurrency": "BRL",
      "quantity": 2,
      "subtotal": 200.00
    }
  ],
  "totalQuantity": 2,
  "totalPrice": 200.00,
  "currency": "BRL",
  "createdAt": "2025-10-18T10:00:00",
  "updatedAt": "2025-10-18T10:00:00"
}
```

---

### 📋 Order Endpoints

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| **GET** | `/api/orders/all` | Listar todos pedidos | - |
| **GET** | `/api/orders/{id}` | Buscar pedido por ID | - |
| **GET** | `/api/orders/user/{userId}` | Listar pedidos do usuário | - |
| **POST** | `/api/orders/{id}/cancel` | ⭐ **Cancelar pedido** | - |

**Resposta OrderDTO:**
```json
{
  "id": 1,
  "userId": 1,
  "totalAmount": 200.00,
  "totalCurrency": "BRL",
  "status": "PENDING",
  "orderDate": "2025-10-18T10:00:00",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "unitPriceAmount": 100.00,
      "unitPriceCurrency": "BRL",
      "totalPrice": 200.00
    }
  ]
}
```

---

## Funcionalidades

### 1. Gerenciamento de Usuários
- ✅ CRUD completo
- ✅ Validação de email único
- ✅ Senha armazenada (adicionar hash em produção)

### 2. Catálogo de Produtos
- ✅ CRUD completo de produtos
- ✅ Preço com moeda configurável
- ✅ Controle de estoque
- ✅ Atualização de estoque via eventos

### 3. Carrinho de Compras
- ✅ Um carrinho por usuário
- ✅ Adicionar produtos (incrementa quantidade se já existe)
- ✅ Cálculo automático de totais
- ✅ Checkout com publicação de evento
- ✅ Limpeza automática após checkout

### 4. Pedidos
- ✅ Criação automática via evento de checkout
- ✅ Listagem por usuário
- ✅ Cancelamento com restauração de estoque
- ✅ Controle de status

### 5. Gerenciamento de Estoque
- ✅ **Não afeta estoque**: Adicionar ao carrinho
- ✅ **Decrementa estoque**: Checkout (via evento)
- ✅ **Incrementa estoque**: Cancelamento (via evento)
- ✅ Validação de estoque insuficiente

---

## Regras de Negócio

### Carrinho

| Ação | Comportamento |
|------|---------------|
| Adicionar produto já existente | Incrementa quantidade |
| Checkout com carrinho vazio | Erro: "Cannot checkout an empty cart" |
| Checkout | Publica evento, limpa carrinho, atualiza updatedAt |

### Estoque

| Ação | Efeito no Estoque |
|------|-------------------|
| Adicionar ao carrinho | ❌ Sem alteração |
| Checkout (criar pedido) | ✅ Decrementa quantidade |
| Cancelar pedido | ✅ Incrementa quantidade (restaura) |
| Estoque insuficiente | ❌ Erro no checkout |

### Pedidos

| Status | Pode Cancelar? | Descrição |
|--------|----------------|-----------|
| PENDING | ✅ Sim | Pedido criado |
| PROCESSING | ✅ Sim | Em processamento |
| SHIPPED | ✅ Sim | Enviado |
| DELIVERED | ❌ Não | Entregue |
| CANCELLED | ❌ Não | Já cancelado |

---

## Como Executar

### Pré-requisitos

- Java 21
- PostgreSQL 12+
- Maven 3.8+

### 1. Configurar Banco de Dados

```bash
# Criar banco
createdb ecommerce

# Configurar variáveis de ambiente
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=sua_senha
export DATABASE_URL=jdbc:postgresql://localhost:5432/ecommerce
```

### 2. Compilar e Executar

```bash
# Compilar
mvn clean install

# Executar
mvn spring-boot:run
```

### 3. Acessar Aplicação

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs

---

## Exemplos de Uso

### 🎯 Fluxo Completo de Compra

#### 1. Criar Usuário
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com",
    "password": "senha123"
  }'

# Resposta: {"id": 1, "name": "João Silva", ...}
```

#### 2. Criar Produto
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Notebook Dell",
    "description": "Notebook i7 16GB",
    "priceAmount": 3500.00,
    "priceCurrency": "BRL",
    "stock": 50
  }'

# Resposta: {"id": 1, "name": "Notebook Dell", "stock": 50, ...}
```

#### 3. Adicionar ao Carrinho
```bash
curl -X POST http://localhost:8080/api/carts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2
  }'

# Resposta: Carrinho com 2 notebooks, total: 7000.00
```

#### 4. Verificar Carrinho
```bash
curl http://localhost:8080/api/carts/user/1

# Resposta: 
# {
#   "items": [{"productId": 1, "quantity": 2, "subtotal": 7000.00}],
#   "totalPrice": 7000.00
# }
```

#### 5. Realizar Checkout
```bash
curl -X POST http://localhost:8080/api/carts/user/1/checkout

# Resposta: Carrinho vazio
# {
#   "items": [],
#   "totalPrice": 0,
#   "updatedAt": "2025-10-18T10:30:00"
# }
```

**O que acontece nos bastidores:**
1. ✅ CheckoutEvent publicado
2. ✅ OrderService cria pedido (assíncrono)
3. ✅ ProductService decrementa estoque (assíncrono)
4. ✅ Carrinho limpo (cart_items deletados)
5. ✅ Cliente recebe resposta imediata

#### 6. Aguardar Processamento
```bash
# Aguardar 2 segundos para eventos serem processados
sleep 2
```

#### 7. Verificar Pedido Criado
```bash
curl http://localhost:8080/api/orders/user/1

# Resposta:
# [
#   {
#     "id": 1,
#     "status": "PENDING",
#     "totalAmount": 7000.00,
#     "items": [{"productId": 1, "quantity": 2}]
#   }
# ]
```

#### 8. Verificar Estoque Decrementado
```bash
curl http://localhost:8080/api/products/1

# Resposta:
# {
#   "id": 1,
#   "name": "Notebook Dell",
#   "stock": 48  ← Decrementado de 50 para 48
# }
```

#### 9. Cancelar Pedido
```bash
curl -X POST http://localhost:8080/api/orders/1/cancel

# Resposta:
# {
#   "id": 1,
#   "status": "CANCELLED",
#   "totalAmount": 7000.00
# }
```

#### 10. Verificar Estoque Restaurado
```bash
# Aguardar eventos
sleep 2

curl http://localhost:8080/api/products/1

# Resposta:
# {
#   "id": 1,
#   "name": "Notebook Dell",
#   "stock": 50  ← Restaurado para 50
# }
```

---

### 🔄 Cenários de Teste

#### Cenário 1: Estoque Insuficiente
```bash
# Produto com estoque = 5
# Tentar comprar 10

curl -X POST http://localhost:8080/api/carts \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productId": 1, "quantity": 10}'

curl -X POST http://localhost:8080/api/carts/user/1/checkout

# Resultado: Erro após 2 segundos
# "Estoque insuficiente para produto: Notebook Dell"
```

#### Cenário 2: Carrinho Vazio
```bash
# Tentar checkout sem itens
curl -X POST http://localhost:8080/api/carts/user/1/checkout

# Resultado: Erro imediato
# "Cannot checkout an empty cart"
```

#### Cenário 3: Cancelar Pedido Entregue
```bash
# Pedido com status DELIVERED
curl -X POST http://localhost:8080/api/orders/1/cancel

# Resultado: Erro
# "Cannot cancel a delivered order"
```

---

## Monitoramento

### Logs da Aplicação

**Durante Checkout:**
```
INFO - Processando atualização de estoque para checkout. Carrinho: 1, Usuário: 1
INFO - Estoque atualizado para produto 1: 50 -> 48 (Decrementado: 2)
INFO - Recebido evento de checkout para o usuário: 1, carrinho: 1
INFO - Pedido criado com sucesso. ID: 1, Usuário: 1, Total: 7000.00 BRL
```

**Durante Cancelamento:**
```
INFO - Iniciando cancelamento do pedido: 1
INFO - Processando restauração de estoque para pedido cancelado. Pedido: 1
INFO - Estoque restaurado para produto 1: 48 -> 50 (Incrementado: 2)
INFO - Pedido 1 cancelado com sucesso. Evento publicado para restaurar estoque
```

### Consultas SQL Úteis

```sql
-- Ver carrinhos ativos
SELECT c.id, c.user_id, COUNT(ci.id) as items
FROM carts c
LEFT JOIN cart_items ci ON c.id = ci.cart_id
GROUP BY c.id, c.user_id;

-- Ver pedidos por status
SELECT status, COUNT(*) as total
FROM orders
GROUP BY status;

-- Ver eventos pendentes
SELECT * FROM event_publication 
WHERE completion_date IS NULL;

-- Ver estoque de produtos
SELECT id, name, stock 
FROM products 
ORDER BY stock ASC;

-- Ver pedidos com itens
SELECT o.id, o.status, o.total_amount, 
       COUNT(oi.id) as item_count
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id;
```

---

## Estrutura do Banco de Dados

### Tabelas Principais

```
users
├── id (PK)
├── name
├── email (UNIQUE)
├── password
└── created_at

products
├── id (PK)
├── name
├── description
├── price_amount
├── price_currency
├── stock
├── created_at
└── updated_at

carts
├── id (PK)
├── user_id (FK → users)
├── created_at
└── updated_at

cart_items
├── id (PK)
├── cart_id (FK → carts)
├── product_id (FK → products)
└── quantity

orders
├── id (PK)
├── user_id (FK → users)
├── total_amount
├── total_currency
├── status
└── order_date

order_items
├── id (PK)
├── order_id (FK → orders)
├── product_id (FK → products)
├── quantity
├── unit_price_amount
└── unit_price_currency

event_publication (Spring Modulith)
├── id (PK)
├── event_type
├── listener_id
├── publication_date
├── serialized_event (TEXT)
└── completion_date
```

---

## Swagger/OpenAPI

A aplicação possui documentação interativa via Swagger UI.

**Acessar:** http://localhost:8080/swagger-ui/index.html

**Recursos:**
- ✅ Testar todos endpoints
- ✅ Ver schemas de DTOs
- ✅ Executar requisições diretamente
- ✅ Ver respostas de erro
- ✅ Exportar para Postman/Insomnia

---

## Próximas Melhorias

### Segurança
- [ ] Implementar Spring Security
- [ ] JWT para autenticação
- [ ] Hash de senhas (BCrypt)
- [ ] Autorização por roles

### Funcionalidades
- [ ] Pagamento (integração)
- [ ] Notificações por email
- [ ] Histórico de preços
- [ ] Cupons de desconto
- [ ] Avaliações de produtos

### Observabilidade
- [ ] Métricas com Micrometer
- [ ] Tracing distribuído
- [ ] Health checks
- [ ] Dashboards

### Testes
- [ ] Testes unitários
- [ ] Testes de integração completos
- [ ] Testes de carga
- [ ] Contract testing

---

## Glossário

**Modular Monolith**: Aplicação monolítica organizada em módulos independentes

**Event-Driven Architecture**: Arquitetura baseada em eventos de domínio

**Spring Modulith**: Framework para construir aplicações modulares com Spring

**Transactional Outbox**: Padrão para garantir consistência eventual

**orphanRemoval**: Recurso JPA que remove entidades órfãs automaticamente

**ApplicationModuleListener**: Anotação do Spring Modulith para listeners assíncronos

**DTO (Data Transfer Object)**: Objeto para transferência de dados entre camadas

**Bounded Context**: Contexto delimitado no Domain-Driven Design

---

## Contato e Suporte

Para dúvidas ou problemas:
1. Consulte esta documentação
2. Verifique os logs da aplicação
3. Consulte a documentação do Spring Modulith: https://docs.spring.io/spring-modulith/reference/

---

## Licença

Este projeto é um exemplo educacional para demonstrar arquitetura modular com Spring Modulith.

---

**📚 Documentação v1.0 - E-commerce Modular Monolith**  
**🚀 Desenvolvido com Spring Boot, Spring Modulith e PostgreSQL**

*Última atualização: Outubro 2025*

