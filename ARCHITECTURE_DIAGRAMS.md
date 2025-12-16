# 🏗️ Diagramas de Arquitetura - E-commerce Modular Monolith

Este documento contém diagramas Mermaid que modelam a arquitetura do sistema de e-commerce modular.

---

## 📊 1. Visão Geral da Arquitetura Modular

```mermaid
graph TB
    subgraph "E-commerce Application (Modular Monolith)"
        direction TB
        
        subgraph "User Module"
            UC[UserController]
            US[UserService]
            UR[UserRepository]
            UE[User Entity]
            UAPI[UserModuleAPI]
        end
        
        subgraph "Product Module"
            PC[ProductController]
            PS[ProductService]
            PR[ProductRepository]
            PE[Product Entity]
            PAPI[ProductModuleAPI]
        end
        
        subgraph "Cart Module"
            CC[CartController]
            CS[CartService]
            CR[CartRepository]
            CE[Cart Entity]
            CI[CartItem Entity]
        end
        
        subgraph "Order Module"
            OC[OrderController]
            OS[OrderService]
            OR[OrderRepository]
            OE[Order Entity]
            OI[OrderItem Entity]
        end
        
        subgraph "Event Module"
            CEV[CheckoutEvent]
            OCEV[OrderCancelledEvent]
        end
        
        subgraph "Auth Module"
            AC[AuthController]
            AS[AuthService]
            JS[JwtService]
            SC[SecurityConfig]
        end
    end
    
    subgraph "Database"
        DB[(PostgreSQL)]
    end
    
    subgraph "External"
        CLIENT[Cliente/REST API]
    end
    
    CLIENT -->|HTTP| UC
    CLIENT -->|HTTP| PC
    CLIENT -->|HTTP| CC
    CLIENT -->|HTTP| OC
    CLIENT -->|HTTP| AC
    
    UC --> US
    US --> UR
    UR --> DB
    UE --> DB
    
    PC --> PS
    PS --> PR
    PR --> DB
    PE --> DB
    
    CC --> CS
    CS --> CR
    CR --> DB
    CE --> DB
    CI --> DB
    
    OC --> OS
    OS --> OR
    OR --> DB
    OE --> DB
    OI --> DB
    
    CS -->|publica| CEV
    CEV -->|escuta| OS
    CEV -->|escuta| PS
    OS -->|publica| OCEV
    OCEV -->|escuta| PS
    
    CS -.->|valida estoque| PAPI
    OS -.->|consulta usuário| UAPI
    
    style CEV fill:#ffd700
    style UEV fill:#ffd700
    style OCEV fill:#ffd700
```

---

## 🔄 2. Fluxo de Checkout (Event-Driven)

```mermaid
sequenceDiagram
    participant Client
    participant CartController
    participant CartService
    participant ProductModuleAPI
    participant CheckoutEvent
    participant OrderService
    participant ProductService
    participant Database
    
    Client->>CartController: POST /carts/user/{id}/checkout
    CartController->>CartService: checkout(userId)
    
    CartService->>CartService: Valida carrinho não vazio
    CartService->>ProductModuleAPI: validateProductsStock(items)
    ProductModuleAPI->>Database: SELECT stock FROM products
    Database-->>ProductModuleAPI: stock data
    ProductModuleAPI-->>CartService: validação OK
    
    CartService->>CartService: Publica CheckoutEvent
    CartService->>CheckoutEvent: publish(event)
    
    CheckoutEvent->>OrderService: @EventListener onCheckoutEvent()
    OrderService->>Database: INSERT INTO orders
    OrderService->>Database: INSERT INTO order_items
    
    CheckoutEvent->>ProductService: @EventListener onCheckoutEvent()
    ProductService->>Database: UPDATE products SET stock = stock - quantity
    
    CartService->>Database: DELETE cart_items (orphanRemoval)
    CartService->>Database: UPDATE carts SET updated_at
    
    CartService-->>CartController: CartDTO (vazio)
    CartController-->>Client: 200 OK (carrinho limpo)
    
    Note over CheckoutEvent,ProductService: Processamento Síncrono<br/>na mesma transação<br/>Validação de estoque antes da publicação
```

---

## ❌ 3. Fluxo de Cancelamento de Pedido

```mermaid
sequenceDiagram
    participant Client
    participant OrderController
    participant OrderService
    participant OrderCancelledEvent
    participant ProductService
    participant Database
    
    Client->>OrderController: POST /orders/{id}/cancel
    OrderController->>OrderService: cancelOrder(orderId)
    
    OrderService->>Database: SELECT * FROM orders WHERE id = ?
    Database-->>OrderService: Order data
    
    OrderService->>OrderService: Valida status (não pode ser DELIVERED)
    OrderService->>Database: UPDATE orders SET status = 'CANCELLED'
    
    OrderService->>OrderCancelledEvent: publish(event)
    
    OrderCancelledEvent->>ProductService: @ApplicationModuleListener onOrderCancelledEvent()
    ProductService->>Database: SELECT * FROM order_items WHERE order_id = ?
    Database-->>ProductService: Order items data
    ProductService->>Database: UPDATE products SET stock = stock + quantity
    
    OrderService-->>OrderController: OrderDTO (status: CANCELLED)
    OrderController-->>Client: 200 OK
    
    Note over OrderCancelledEvent,ProductService: Processamento Assíncrono<br/>via Spring Modulith
```

---

## 🏛️ 4. Estrutura de Camadas por Módulo

```mermaid
graph TB
    subgraph "User Module"
        direction TB
        UC1[UserController<br/>REST API]
        US1[UserService<br/>Business Logic]
        UR1[UserRepository<br/>Data Access]
        UE1[User Entity<br/>JPA]
        UAPI1[UserModuleAPI<br/>Public API]
    end
    
    subgraph "Product Module"
        direction TB
        PC1[ProductController<br/>REST API]
        PS1[ProductService<br/>Business Logic<br/>+ Event Listeners]
        PR1[ProductRepository<br/>Data Access]
        PE1[Product Entity<br/>JPA]
        PAPI1[ProductModuleAPI<br/>Public API]
    end
    
    subgraph "Cart Module"
        direction TB
        CC1[CartController<br/>REST API]
        CS1[CartService<br/>Business Logic<br/>+ Checkout]
        CR1[CartRepository<br/>Data Access]
        CE1[Cart Entity<br/>JPA]
        CI1[CartItem Entity<br/>JPA]
    end
    
    subgraph "Order Module"
        direction TB
        OC1[OrderController<br/>REST API]
        OS1[OrderService<br/>Business Logic<br/>+ Event Listeners]
        OR1[OrderRepository<br/>Data Access]
        OE1[Order Entity<br/>JPA]
        OI1[OrderItem Entity<br/>JPA]
    end
    
    UC1 --> US1
    US1 --> UR1
    UR1 --> UE1
    US1 --> UAPI1
    
    PC1 --> PS1
    PS1 --> PR1
    PR1 --> PE1
    PS1 --> PAPI1
    
    CC1 --> CS1
    CS1 --> CR1
    CR1 --> CE1
    CE1 --> CI1
    
    OC1 --> OS1
    OS1 --> OR1
    OR1 --> OE1
    OE1 --> OI1
    
    style PS1 fill:#90EE90
    style CS1 fill:#90EE90
    style OS1 fill:#90EE90
```

---

## 🗄️ 5. Modelo de Dados (Database Schema)

```mermaid
erDiagram
    USERS ||--o{ CARTS : "tem"
    USERS ||--o{ ORDERS : "faz"
    CARTS ||--o{ CART_ITEMS : "contém"
    PRODUCTS ||--o{ CART_ITEMS : "referenciado"
    PRODUCTS ||--o{ ORDER_ITEMS : "referenciado"
    ORDERS ||--o{ ORDER_ITEMS : "contém"
    
    USERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar password
        timestamp created_at
    }
    
    PRODUCTS {
        bigint id PK
        varchar name
        text description
        decimal price_amount
        varchar price_currency
        int stock
        timestamp created_at
        timestamp updated_at
    }
    
    CARTS {
        bigint id PK
        bigint user_id FK
        timestamp created_at
        timestamp updated_at
    }
    
    CART_ITEMS {
        bigint id PK
        bigint cart_id FK
        bigint product_id FK
        int quantity
    }
    
    ORDERS {
        bigint id PK
        bigint user_id FK
        decimal total_amount
        varchar total_currency
        varchar status
        timestamp order_date
    }
    
    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        int quantity
        decimal unit_price_amount
        varchar unit_price_currency
    }
    
    EVENT_PUBLICATION {
        bigint id PK
        varchar event_type
        varchar listener_id
        timestamp publication_date
        text serialized_event
        timestamp completion_date
    }
```

---

## 📡 6. Comunicação entre Módulos via Eventos

```mermaid
graph LR
    subgraph "Módulos"
        CART[Cart Module]
        ORDER[Order Module]
        PRODUCT[Product Module]
    end
    
    subgraph "Eventos"
        CEV[CheckoutEvent]
        OCEV[OrderCancelledEvent]
    end
    
    CART -->|1. Publica| CEV
    CEV -->|2. Consumido por| ORDER
    CEV -->|3. Consumido por| PRODUCT
    ORDER -->|4. Publica| OCEV
    OCEV -->|5. Consumido por| PRODUCT
    
    style CEV fill:#FFE4B5
    style UEV fill:#FFE4B5
    style OCEV fill:#FFE4B5
    style CART fill:#E6F3FF
    style ORDER fill:#E6F3FF
    style PRODUCT fill:#E6F3FF
```

---

## 🔐 7. Arquitetura de Segurança (Auth Module)

```mermaid
graph TB
    subgraph "Auth Module"
        AC2[AuthController]
        AS2[AuthService]
        JS2[JwtService]
        CUS[CurrentUserService]
        SC2[SecurityConfig]
        JAF[JwtAuthenticationFilter]
    end
    
    subgraph "User Module"
        UR2[UserRepository]
        UE2[User Entity]
    end
    
    CLIENT2[Cliente] -->|POST /auth/login| AC2
    CLIENT2 -->|POST /auth/register| AC2
    CLIENT2 -->|Requisições Autenticadas| JAF
    
    AC2 --> AS2
    AS2 --> JS2
    AS2 --> UR2
    UR2 --> UE2
    AS2 --> CUS
    
    JAF --> JS2
    JAF --> CUS
    SC2 --> JAF
    
    style JS2 fill:#FFB6C1
    style JAF fill:#FFB6C1
    style SC2 fill:#FFB6C1
```

---

## 🎯 8. Fluxo Completo de Compra (End-to-End)

```mermaid
flowchart TD
    START([Cliente inicia compra]) --> CREATE_USER[Criar Usuário]
    CREATE_USER --> CREATE_PRODUCT[Criar Produto]
    CREATE_PRODUCT --> ADD_TO_CART[Adicionar ao Carrinho]
    ADD_TO_CART --> CHECK_CART{Verificar Carrinho}
    CHECK_CART -->|Vazio| ADD_TO_CART
    CHECK_CART -->|Com itens| CHECKOUT[Realizar Checkout]
    
    CHECKOUT --> VALIDATE_STOCK[Validar Estoque]
    VALIDATE_STOCK -->|Insuficiente| ERROR_STOCK[Erro: Estoque Insuficiente]
    VALIDATE_STOCK -->|Suficiente| PUBLISH_CHECKOUT[Publicar CheckoutEvent]
    
    PUBLISH_CHECKOUT --> CREATE_ORDER[OrderService: Criar Pedido]
    PUBLISH_CHECKOUT --> UPDATE_STOCK[ProductService: Decrementar Estoque]
    CREATE_ORDER --> CLEAR_CART[Limpar Carrinho]
    UPDATE_STOCK --> CLEAR_CART
    CLEAR_CART --> ORDER_CREATED[Pedido Criado]
    
    ORDER_CREATED --> CANCEL_OPTION{Cancelar Pedido?}
    CANCEL_OPTION -->|Sim| VALIDATE_STATUS{Status permitido?}
    CANCEL_OPTION -->|Não| END([Fim])
    
    VALIDATE_STATUS -->|DELIVERED| ERROR_CANCEL[Erro: Não pode cancelar]
    VALIDATE_STATUS -->|Outros| CANCEL_ORDER[Cancelar Pedido]
    CANCEL_ORDER --> PUBLISH_CANCEL[Publicar OrderCancelledEvent]
    PUBLISH_CANCEL --> RESTORE_STOCK[ProductService: Restaurar Estoque]
    RESTORE_STOCK --> END
    
    ERROR_STOCK --> END
    ERROR_CANCEL --> END
    
    style CHECKOUT fill:#90EE90
    style CREATE_ORDER fill:#90EE90
    style UPDATE_STOCK fill:#90EE90
    style CANCEL_ORDER fill:#FFB6C1
    style RESTORE_STOCK fill:#FFB6C1
```

---

## 📦 9. Dependências entre Módulos

```mermaid
graph TD
    subgraph "Módulos Core"
        USER[User Module]
        PRODUCT[Product Module]
        CART[Cart Module]
        ORDER[Order Module]
    end
    
    subgraph "Módulos de Suporte"
        EVENT[Event Module]
        AUTH[Auth Module]
        COMMON[Common Module]
        EXCEPTION[Exception Module]
    end
    
    CART -.->|usa| PRODUCT
    CART -.->|usa| USER
    ORDER -.->|usa| USER
    ORDER -.->|usa| PRODUCT
    
    CART -->|publica| EVENT
    ORDER -->|publica| EVENT
    PRODUCT -->|escuta| EVENT
    
    AUTH -->|usa| USER
    AUTH -->|usa| COMMON
    
    USER -->|usa| COMMON
    PRODUCT -->|usa| COMMON
    CART -->|usa| COMMON
    ORDER -->|usa| COMMON
    
    USER -->|usa| EXCEPTION
    PRODUCT -->|usa| EXCEPTION
    CART -->|usa| EXCEPTION
    ORDER -->|usa| EXCEPTION
    
    style EVENT fill:#FFD700
    style COMMON fill:#D3D3D3
    style EXCEPTION fill:#D3D3D3
```

---

## 🔧 10. Stack Tecnológica

```mermaid
graph TB
    subgraph "Aplicação"
        APP[EcommerceApplication<br/>Spring Boot 3.5.6]
    end
    
    subgraph "Framework"
        SB[Spring Boot]
        SM[Spring Modulith 1.4.1]
        SDJ[Spring Data JPA]
        SW[Spring Web]
    end
    
    subgraph "Persistência"
        JPA[JPA/Hibernate]
        PG[(PostgreSQL)]
    end
    
    subgraph "Segurança"
        SS[Spring Security]
        JWT[JWT]
    end
    
    subgraph "Documentação"
        OAPI[OpenAPI 3]
        SWAGGER[Swagger UI]
    end
    
    subgraph "Build"
        MAVEN[Maven]
        JAVA[Java 21]
    end
    
    APP --> SB
    SB --> SM
    SB --> SDJ
    SB --> SW
    SB --> SS
    
    SDJ --> JPA
    JPA --> PG
    
    SS --> JWT
    
    SW --> OAPI
    OAPI --> SWAGGER
    
    APP --> MAVEN
    MAVEN --> JAVA
    
    style APP fill:#90EE90
    style SM fill:#87CEEB
    style PG fill:#4169E1
```

---

## 📝 Notas sobre os Diagramas

### Tipos de Comunicação

1. **Síncrona (REST API)**: Comunicação direta entre módulos via interfaces públicas (ModuleAPI)
2. **Event-Driven Síncrono**: Eventos processados na mesma transação usando `@EventListener`
3. **Event-Driven Assíncrono**: Eventos processados de forma assíncrona usando `@ApplicationModuleListener`

### Padrões Arquiteturais

- **Modular Monolith**: Aplicação monolítica organizada em módulos independentes
- **Event-Driven Architecture**: Comunicação via eventos de domínio
- **Domain-Driven Design**: Bounded contexts bem definidos
- **Repository Pattern**: Abstração de acesso a dados
- **DTO Pattern**: Transferência de dados entre camadas
- **Transactional Outbox**: Garantia de entrega de eventos

### Fluxos Principais

1. **Checkout**: Cart → CheckoutEvent → (Order + Product) - ambos consomem o mesmo evento
2. **Cancelamento**: Order → OrderCancelledEvent → Product
3. **Autenticação**: Client → AuthController → AuthService → UserRepository

**Nota sobre Checkout:** A validação de estoque ocorre antes da publicação do evento, garantindo segurança. Order e Product consomem o CheckoutEvent diretamente, simplificando a arquitetura.

---

**📚 Documentação gerada para E-commerce Modular Monolith**  
**🔄 Última atualização: Outubro 2025**



