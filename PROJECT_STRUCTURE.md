# 📁 Estrutura do Projeto

## 🗂️ Visão Geral

```
ecommerce/
├── 📚 Documentação
│   ├── README.md                    ← Início aqui
│   ├── API_DOCUMENTATION.md         ← Documentação completa ⭐
│   ├── DOCS_INDEX.md                ← Índice de documentação
│   ├── PROJECT_STRUCTURE.md         ← Este arquivo
│   ├── CART_CLEANUP_FIX.md          ← Detalhes técnicos
│   └── QUICK_SUMMARY.md             ← Resumo rápido
│
├── 🔧 Configuração
│   ├── pom.xml                      ← Dependências Maven
│   ├── application.properties       ← Configurações da aplicação
│   └── .env                         ← Variáveis de ambiente (não versionado)
│
├── 💻 Código Fonte
│   └── src/main/java/com/modulith/ecommerce/
│       ├── EcommerceApplication.java
│       ├── user/                    ← Módulo de Usuários
│       ├── product/                 ← Módulo de Produtos
│       ├── cart/                    ← Módulo de Carrinho
│       ├── order/                   ← Módulo de Pedidos
│       └── event/                   ← Eventos de Domínio
│
├── 🧪 Testes
│   └── src/test/java/
│
└── 🗄️ Banco de Dados
    └── PostgreSQL (ecommerce)
```

## 📚 Documentação (Raiz do Projeto)

### Essenciais
```
README.md                     - Início, quick start, overview
API_DOCUMENTATION.md          - Documentação COMPLETA ⭐
DOCS_INDEX.md                 - Índice de toda documentação
```

### Técnicos
```
PROJECT_STRUCTURE.md          - Estrutura do projeto (este arquivo)
CART_CLEANUP_FIX.md           - Detalhes da limpeza do carrinho
QUICK_SUMMARY.md              - Resumo das mudanças recentes
```

### Configuração
```
pom.xml                       - Maven dependencies
application.properties        - Configurações Spring
```

## 💻 Código Fonte

### Estrutura Completa

```
src/main/java/com/modulith/ecommerce/
│
├── EcommerceApplication.java                    [Classe principal]
│
├── 👤 user/                                     [Módulo de Usuários]
│   ├── User.java                                [Entidade]
│   ├── UserDTO.java                             [DTO de leitura]
│   ├── UserCreateDTO.java                       [DTO de criação]
│   ├── UserRepository.java                      [Repositório]
│   ├── UserService.java                         [Lógica de negócio]
│   ├── UserController.java                      [API REST]
│   └── UserModuleAPI.java                       [API pública do módulo]
│
├── 📦 product/                                  [Módulo de Produtos]
│   ├── Product.java                             [Entidade]
│   ├── ProductDTO.java                          [DTO de leitura]
│   ├── CreateProductDTO.java                    [DTO de criação]
│   ├── ProductRepository.java                   [Repositório]
│   ├── ProductService.java                      [Lógica + Event Listeners] ⭐
│   ├── ProductController.java                   [API REST]
│   └── ProductModuleAPI.java                    [API pública do módulo]
│
├── 🛒 cart/                                     [Módulo de Carrinho]
│   ├── Cart.java                                [Entidade]
│   ├── CartItem.java                            [Entidade]
│   ├── CartDTO.java                             [DTO de leitura]
│   ├── CartItemDTO.java                         [DTO de item]
│   ├── addCartItemDTO.java                      [DTO de adição]
│   ├── CartRepository.java                      [Repositório]
│   ├── CartService.java                         [Lógica + Checkout] ⭐
│   └── CartController.java                      [API REST]
│
├── 📋 order/                                    [Módulo de Pedidos]
│   ├── Order.java                               [Entidade]
│   ├── OrderItem.java                           [Entidade]
│   ├── OrderStatus.java                         [Enum]
│   ├── OrderDTO.java                            [DTO de leitura]
│   ├── OrderItemDTO.java                        [DTO de item]
│   ├── OrderRepository.java                     [Repositório]
│   ├── OrderService.java                        [Lógica + Event Listeners] ⭐
│   └── OrderController.java                     [API REST]
│
└── 📡 event/                                    [Eventos de Domínio]
    ├── CheckoutEvent.java                       [Evento de checkout] ⭐
    ├── UpdateEvent.java                         [Evento de atualização de estoque] ⭐
    └── OrderCancelledEvent.java                 [Evento de cancelamento] ⭐
```

## 🔑 Arquivos Principais

### 1. EcommerceApplication.java
```java
@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
```
**Função:** Ponto de entrada da aplicação

---

### 2. CheckoutEvent.java ⭐
```java
public record CheckoutEvent(
    Long cart,
    Long user,
    List<CheckoutItem> items
) {
    public record CheckoutItem(
        Long product,
        Integer quantity
    ) {}
}
```
**Função:** Evento publicado quando checkout é realizado  
**Consumido por:** OrderService (cria pedido e publica UpdateEvent)

### 2.1. UpdateEvent.java ⭐
```java
public record UpdateEvent(
    Long cart,
    Long user,
    Map<Long, Integer> productQuantities
) {}
```
**Função:** Evento publicado pelo OrderService para atualização de estoque  
**Consumido por:** ProductService (decrementa estoque)

---

### 3. OrderCancelledEvent.java ⭐
```java
public record OrderCancelledEvent(
    Long orderId,
    Long userId,
    List<CancelledItem> items,
    LocalDateTime cancelledDate
)
```
**Função:** Evento publicado quando pedido é cancelado  
**Consumido por:** ProductService

---

### 4. CartService.checkout() ⭐
```java
@Transactional
public CartDTO checkout(Long userId) {
    // 1. Valida carrinho
    // 2. Publica CheckoutEvent
    // 3. Limpa carrinho
    // 4. Atualiza updatedAt
}
```
**Função:** Realiza checkout e publica evento

---

### 5. OrderService.onCheckoutEvent() ⭐
```java
@EventListener
public void onCheckoutEvent(CheckoutEvent event) {
    // Cria pedido a partir do evento
    // Publica UpdateEvent para atualização de estoque
}
```
**Função:** Listener que cria pedido automaticamente (processamento síncrono)

---

### 6. ProductService.onCheckoutEvent() ⭐
```java
@EventListener
public void onCheckoutEvent(UpdateEvent event) {
    // Decrementa estoque dos produtos (batch update)
}
```
**Função:** Listener que atualiza estoque no checkout (processamento síncrono)

---

### 7. ProductService.onOrderCancelledEvent() ⭐
```java
@ApplicationModuleListener
public void onOrderCancelledEvent(OrderCancelledEvent event) {
    // Incrementa estoque dos produtos
}
```
**Função:** Listener que restaura estoque no cancelamento

## 🗄️ Estrutura do Banco de Dados

### Tabelas da Aplicação

```
┌─────────────┐
│   users     │
├─────────────┤
│ id          │←─┐
│ name        │  │
│ email       │  │
│ password    │  │
│ created_at  │  │
└─────────────┘  │
                 │
┌─────────────┐  │
│  products   │  │
├─────────────┤  │
│ id          │←─┼─┐
│ name        │  │ │
│ description │  │ │
│ price_amt   │  │ │
│ price_curr  │  │ │
│ stock       │  │ │ ← Atualizado por eventos
│ created_at  │  │ │
│ updated_at  │  │ │
└─────────────┘  │ │
                 │ │
┌─────────────┐  │ │
│   carts     │  │ │
├─────────────┤  │ │
│ id          │←─┼─┼─┐
│ user_id     │──┘ │ │
│ created_at  │    │ │
│ updated_at  │    │ │ ← Atualizado no checkout
└─────────────┘    │ │
       ↑           │ │
       │           │ │
┌─────────────┐    │ │
│ cart_items  │    │ │
├─────────────┤    │ │
│ id          │    │ │
│ cart_id     │────┘ │
│ product_id  │──────┘
│ quantity    │
└─────────────┘
       ↓
  Deletados após checkout (orphanRemoval)
  
┌─────────────┐
│   orders    │
├─────────────┤
│ id          │←─┐
│ user_id     │──┼─────→ users
│ total_amt   │  │
│ total_curr  │  │
│ status      │  │ ← PENDING, CANCELLED, etc
│ order_date  │  │
└─────────────┘  │
       ↑         │
       │         │
┌─────────────┐  │
│order_items  │  │
├─────────────┤  │
│ id          │  │
│ order_id    │──┘
│ product_id  │──────→ products
│ quantity    │
│ unit_price  │
└─────────────┘

┌──────────────────┐
│event_publication│  [Spring Modulith]
├──────────────────┤
│ id               │
│ event_type       │ ← CheckoutEvent, OrderCancelledEvent
│ listener_id      │
│ publication_date │
│ serialized_event │ ← JSON do evento
│ completion_date  │ ← NULL = pendente
└──────────────────┘
```

## 🔄 Fluxo de Dados

### Checkout Flow

```
1. Cliente
   ↓ POST /api/carts/user/1/checkout
2. CartController
   ↓ checkout(userId)
3. CartService
   ├─ Valida carrinho
   ├─ Valida estoque (validateProductsStock - batch)
   ├─ Publica CheckoutEvent
   ├─ Limpa cart_items (orphanRemoval)
   └─ Atualiza carts.updated_at
   ↓
4. OrderService.onCheckoutEvent() (@EventListener - síncrono)
   ├─ Cria Order e OrderItems
   └─ Publica UpdateEvent
   ↓
5. ProductService.onCheckoutEvent() (@EventListener - síncrono)
   └─ UPDATE products SET stock = stock - quantity (batch)
```

**Nota:** Processamento síncrono garante que falhas causem rollback completo da transação.

### Cancel Flow

```
1. Cliente
   ↓ POST /api/orders/1/cancel
2. OrderController
   ↓ cancelOrder(id)
3. OrderService
   ├─ UPDATE orders SET status = 'CANCELLED'
   └─ Publica OrderCancelledEvent → event_publication
   ↓
4. Spring Modulith
   └─→ ProductService.onOrderCancelledEvent()
       └─ UPDATE products SET stock = stock + quantity
```

## 📦 Dependências Principais (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Modulith -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-jpa</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    
    <!-- Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>
```

## ⚙️ Configuração (application.properties)

```properties
# Application
spring.application.name=ecommerce

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=postgres
spring.datasource.password=${DATABASE_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server
server.port=8080
```

## 🎯 Pontos de Entrada

### REST API
```
http://localhost:8080
├── users/
├── products/
├── carts/
└── orders/
```

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Docs
```
http://localhost:8080/v3/api-docs
```

## 📊 Métricas do Projeto

### Estatísticas
- **Módulos**: 5 (User, Product, Cart, Order, Event)
- **Endpoints**: ~20 REST endpoints
- **Entidades**: 6 (User, Product, Cart, CartItem, Order, OrderItem)
- **Eventos**: 2 (CheckoutEvent, OrderCancelledEvent)
- **Listeners**: 3 (ProductService×2, OrderService×1)
- **Banco de Dados**: 7 tabelas + 1 event_publication

### Linhas de Código (aprox.)
- **Controllers**: ~150 linhas
- **Services**: ~400 linhas
- **Entities**: ~250 linhas
- **DTOs**: ~200 linhas
- **Events**: ~50 linhas
- **Total**: ~1000 linhas de código

## 🔍 Como Navegar no Código

### Para entender um módulo
1. Comece pela entidade (`User.java`)
2. Veja o DTO (`UserDTO.java`)
3. Entenda o Repository (`UserRepository.java`)
4. Leia o Service (`UserService.java`)
5. Veja o Controller (`UserController.java`)

### Para entender eventos
1. Veja a definição (`CheckoutEvent.java`)
2. Encontre quem publica (`CartService.checkout()`)
3. Encontre quem escuta (`@ApplicationModuleListener`)
4. Trace o fluxo completo

### Para debugar problemas
1. Ative SQL logs (`spring.jpa.show-sql=true`)
2. Verifique tabela `event_publication`
3. Procure por `@ApplicationModuleListener` nos services
4. Acompanhe os logs da aplicação

## 📝 Convenções

### Nomenclatura
- **Entidades**: Singular (User, Product)
- **DTOs**: NomeTipoDTO (UserDTO, CreateProductDTO)
- **Repositories**: NomeRepository (UserRepository)
- **Services**: NomeService (UserService)
- **Controllers**: NomeController (UserController)
- **Eventos**: VerboPastEvent (CheckoutEvent, OrderCancelledEvent)

### Pacotes
- Cada módulo em seu próprio pacote
- DTOs no mesmo pacote do módulo
- Eventos em pacote separado (compartilhado)

### Anotações
- `@Service` para serviços
- `@RestController` para controllers
- `@Repository` para repositories
- `@EventListener` para event listeners síncronos (checkout)
- `@ApplicationModuleListener` para event listeners assíncronos (cancelamento)
- `@Transactional` para métodos transacionais

---

**📁 Estrutura organizada seguindo princípios de Modular Monolith e DDD**

*Para navegação completa da documentação, veja [DOCS_INDEX.md](DOCS_INDEX.md)*

