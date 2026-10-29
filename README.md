# 🛍️ E-commerce Modular Monolith

Sistema de e-commerce desenvolvido com **Spring Modulith** e arquitetura orientada a eventos.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green.svg)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-1.4.1-blue.svg)](https://spring.io/projects/spring-modulith)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)

## 📋 Visão Geral

Aplicação modular que implementa um e-commerce completo com:
- ✅ Gerenciamento de produtos com estoque
- ✅ Carrinho de compras
- ✅ Sistema de pedidos
- ✅ Atualização automática de estoque via eventos
- ✅ Arquitetura event-driven
- ✅ Processamento síncrono com garantia de consistência transacional

## 🏗️ Arquitetura

```
┌─────────┐     ┌──────────┐     ┌─────────┐     ┌─────────┐
│  User   │     │ Product  │     │  Cart   │     │  Order  │
│ Module  │     │  Module  │     │ Module  │     │ Module  │
└─────────┘     └────┬─────┘     └────┬────┘     └────┬────┘
                     │                │               │
                     │    Events      │               │
                     └────────────────┴───────────────┘
                      CheckoutEvent, OrderCancelledEvent
```

### Módulos

- **User**: Gerenciamento de usuários
- **Product**: Catálogo e estoque
- **Cart**: Carrinho de compras
- **Order**: Pedidos e cancelamentos
- **Event**: Eventos de domínio

## 🚀 Quick Start

### Pré-requisitos

- Java 21
- PostgreSQL 12+
- Maven 3.8+

### Configuração

```bash
# 1. Criar banco de dados
createdb ecommerce

# 2. Configurar variáveis de ambiente
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=sua_senha
export DATABASE_URL=jdbc:postgresql://localhost:5432/ecommerce

# 3. Compilar e executar
mvn clean install
mvn spring-boot:run
```

### Acessar

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

## 📚 Documentação

📖 **[Documentação Completa](API_DOCUMENTATION.md)** - Overview, rotas, exemplos e regras de negócio

## 🎯 Funcionalidades Principais

### 1. Carrinho de Compras
```bash
# Adicionar produto
POST /carts
{
  "userId": 1,
  "productId": 1,
  "quantity": 2
}

# Fazer checkout
POST /carts/user/1/checkout
```

### 2. Gerenciamento de Estoque

| Ação | Efeito no Estoque |
|------|-------------------|
| Adicionar ao carrinho | Sem alteração |
| Checkout | Decrementa (via evento) |
| Cancelar pedido | Incrementa (via evento) |

### 3. Pedidos
```bash
# Listar pedidos do usuário
GET /orders/user/1

# Cancelar pedido
POST /orders/1/cancel
```

## 🔄 Fluxo de Eventos

### Checkout
```
Cliente → Checkout → CheckoutEvent
                    ├─► OrderService (cria pedido)
                    └─► ProductService (decrementa estoque)
```
**Nota:** A validação de estoque ocorre antes da publicação do evento. Order e Product consomem o CheckoutEvent diretamente.

### Cancelamento
```
Cliente → Cancel → OrderCancelledEvent
                  └─► ProductService (incrementa estoque)
```

## 📊 API Endpoints

### Usuários
- `POST /users` - Criar usuário
- `GET /users/{id}` - Buscar usuário
- `GET /users` - Listar usuários

### Produtos
- `POST /products` - Criar produto
- `GET /products/{id}` - Buscar produto
- `GET /products` - Listar produtos
- `PATCH /products/{id}/stock?stock={n}` - Atualizar estoque

### Carrinho
- `POST /carts` - Adicionar item
- `GET /carts/user/{userId}` - Buscar carrinho
- `POST /carts/user/{userId}/checkout` - **Checkout**

### Pedidos
- `GET /orders/user/{userId}` - Listar pedidos
- `GET /orders/{id}` - Buscar pedido
- `POST /orders/{id}/cancel` - **Cancelar pedido**

## 🧪 Exemplo de Uso

```bash
# 1. Criar usuário
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name": "João", "email": "joao@email.com", "password": "123"}'

# 2. Criar produto
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Notebook", "priceAmount": 3000, "priceCurrency": "BRL", "stock": 10}'

# 3. Adicionar ao carrinho
curl -X POST http://localhost:8080/carts \
  -H "Content-Type: application/json" \
  -d '{"user": 1, "product": 1, "quantity": 2}'

# 4. Fazer checkout
curl -X POST http://localhost:8080/carts/user/1/checkout

# 5. Verificar pedido (aguardar 2s)
sleep 2
curl http://localhost:8080/orders/user/1
```

## 🛠️ Tecnologias

- **Java 21** - Linguagem
- **Spring Boot 3.5.6** - Framework
- **Spring Modulith 1.4.1** - Arquitetura modular
- **Spring Data JPA** - Persistência
- **PostgreSQL** - Banco de dados
- **Lombok** - Redução de boilerplate
- **Springdoc OpenAPI** - Documentação Swagger

## 📖 Padrões Implementados

- ✅ **Domain-Driven Design (DDD)** - Bounded contexts
- ✅ **Event-Driven Architecture** - Comunicação via eventos
- ✅ **CQRS** - Separação de comandos e queries
- ✅ **Transactional Consistency** - Eventos processados na mesma transação (@EventListener)
- ✅ **Repository Pattern** - Abstração de dados
- ✅ **DTO Pattern** - Transferência de dados

## 📝 Regras de Negócio

### Carrinho
- Um usuário tem um carrinho
- Adicionar produto existente incrementa quantidade
- Checkout limpa o carrinho automaticamente

### Estoque
- **Adicionar ao carrinho**: Não afeta estoque
- **Checkout**: Valida estoque antes da publicação do evento e decrementa automaticamente (via CheckoutEvent)
- **Cancelar pedido**: Restaura estoque automaticamente (via OrderCancelledEvent)

### Pedidos
- Status: `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`
- Não pode cancelar pedidos entregues
- Cancelamento restaura estoque via evento

## 🔍 Monitoramento

### Logs
```
INFO - Processando atualização de estoque para checkout
INFO - Estoque atualizado para produto 1: 10 -> 8
INFO - Pedido criado com sucesso. ID: 1
```

### Eventos
```sql
-- Ver eventos processados
SELECT * FROM event_publication 
WHERE completion_date IS NOT NULL;
```

## 📚 Documentação Adicional

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Documentação completa da API

## 🎓 Recursos de Aprendizado

- [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Event-Driven Architecture](https://docs.spring.io/spring-modulith/reference/events.html)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)

## 🤝 Contribuindo

Este é um projeto educacional demonstrando arquitetura modular com Spring Modulith.

## 📄 Licença

Projeto educacional para demonstração de conceitos de arquitetura de software.

---

**🚀 Desenvolvido com Spring Boot e Spring Modulith**

*Para documentação detalhada, consulte [API_DOCUMENTATION.md](API_DOCUMENTATION.md)*

**🔎  Sobre a pesquisa**

Este repositório reflete o conteúdo gerado pelo seguinte artigo: "Uma Análise Comparativa entre Arquitetura Monolítica Modular
e Microsserviços em Aplicações Web", publicado no VEM 2026/CBSOFT.

https://sol.sbc.org.br/index.php/vem/article/view/46394

Como citar:

ABNT

RAULINO, Gabriel; MARQUES, Allan; BEZERRA, Carla; COUTINHO, Emanuel. Uma Análise Comparativa entre Arquitetura Monolítica Modular e Microsserviços em Aplicações Web. In: WORKSHOP DE VISUALIZAÇÃO, EVOLUÇÃO E MANUTENÇÃO DE SOFTWARE (VEM), 14. , 2026, São Paulo/SP. Anais [...]. Porto Alegre: Sociedade Brasileira de Computação, 2026 . p. 48-54. DOI: https://doi.org/10.5753/vem.2026.32795.

BIBTEX
```bib
@inproceedings{vem,
 author = {Gabriel Raulino and Allan Marques and Carla Bezerra and Emanuel Coutinho},
 title = { Uma Análise Comparativa entre Arquitetura Monolítica Modular e Microsserviços em Aplicações Web},
 booktitle = {Anais do XIV Workshop de Visualização, Evolução e Manutenção de Software},
 location = {São Paulo/SP},
 year = {2026},
 keywords = {},
 issn = {0000-0000},
 pages = {48--54},
 publisher = {SBC},
 address = {Porto Alegre, RS, Brasil},
 doi = {10.5753/vem.2026.32795},
 url = {https://sol.sbc.org.br/index.php/vem/article/view/46394}
}
```
