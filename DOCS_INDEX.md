# 📚 Índice da Documentação

## 📖 Documentação Principal

### [README.md](README.md)
**Visão Geral do Projeto**
- Quick start
- Tecnologias
- Funcionalidades principais
- Exemplos básicos

### [API_DOCUMENTATION.md](API_DOCUMENTATION.md) ⭐
**Documentação Completa da API**
- Overview detalhado da aplicação
- Arquitetura e módulos
- Todos os endpoints com exemplos
- Regras de negócio
- Fluxos completos de uso
- Estrutura do banco de dados
- Monitoramento e logs

## 📊 Como Usar Esta Documentação

### Se você é novo no projeto
1. Comece pelo **[README.md](README.md)**
2. Leia a **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** completa
3. Explore os endpoints no Swagger UI

### Se você quer entender a API
1. Vá direto para **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)**
2. Veja a seção "API Endpoints"
3. Teste os exemplos fornecidos

### Se você quer entender a arquitetura
1. Leia "Arquitetura" em **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)**
2. Veja "Eventos de Domínio"
3. Entenda os fluxos de checkout e cancelamento

### Se você está debugando
1. Consulte "Monitoramento" em **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)**
2. Veja os logs esperados
3. Consulte as queries SQL úteis

## 🎯 Documentação por Funcionalidade

### Usuários
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-user-endpoints)
- CRUD de usuários
- Endpoints disponíveis

### Produtos
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-product-endpoints)
- Gerenciamento de produtos
- Controle de estoque
- Atualização via eventos

### Carrinho de Compras
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-cart-endpoints)
- Adicionar itens
- Checkout
- Limpeza automática

🔧 [CART_CLEANUP_FIX.md](CART_CLEANUP_FIX.md)
- Detalhes técnicos da limpeza

### Pedidos
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-order-endpoints)
- Criação via eventos
- Cancelamento
- Status de pedido

### Gerenciamento de Estoque
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#regras-de-negócio)
- Regras de atualização
- Fluxos de eventos
- Validações

## 🔄 Documentação por Conceito

### Event-Driven Architecture
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#eventos-de-domínio)
- CheckoutEvent
- UpdateEvent
- OrderCancelledEvent
- Fluxos de eventos

### Spring Modulith
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#arquitetura)
- Módulos da aplicação
- Comunicação entre módulos
- @EventListener (processamento síncrono)
- @ApplicationModuleListener (processamento assíncrono)

### Domain-Driven Design
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#módulos)
- Bounded contexts
- Agregados
- Eventos de domínio

## 🛠️ Guias Práticos

### Setup Inicial
📖 [README.md](README.md#-quick-start)
- Instalação
- Configuração
- Primeiro run

### Exemplos de Uso
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#exemplos-de-uso)
- Fluxo completo de compra
- Cenários de teste
- Casos de erro

### Testes
📖 [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-cenários-de-teste)
- Estoque insuficiente
- Carrinho vazio
- Pedido entregue

## 🔍 Referência Rápida

### Comandos Úteis
```bash
# Iniciar aplicação
mvn spring-boot:run

# Acessar Swagger
open http://localhost:8080/swagger-ui/index.html

# Ver logs
tail -f logs/application.log
```

### Endpoints Mais Usados
```bash
# Adicionar ao carrinho
POST /carts

# Fazer checkout
POST /carts/user/{user}/checkout

# Listar pedidos
GET /orders/user/{user}

# Cancelar pedido
POST /orders/{id}/cancel
```

### Queries SQL Úteis
```sql
-- Ver carrinhos ativos
SELECT * FROM carts WHERE id IN (
  SELECT DISTINCT cart_id FROM cart_items
);

-- Ver eventos pendentes
SELECT * FROM event_publication 
WHERE completion_date IS NULL;

-- Ver estoque de produtos
SELECT id, name, stock FROM products;
```

## 📱 Acesso Rápido

### URLs da Aplicação
- **API Base**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Recursos Online
- [Spring Modulith Docs](https://docs.spring.io/spring-modulith/reference/)
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)

## 🎓 Para Aprender Mais

### Conceitos
- **Modular Monolith**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#overview)
- **Event-Driven**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#eventos-de-domínio)
- **DDD**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#módulos)

### Implementação
- **Listeners de Eventos**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#2-product-module-produtos)
- **Gerenciamento de Transações**: [CART_CLEANUP_FIX.md](CART_CLEANUP_FIX.md)
- **JPA Relationships**: [CART_CLEANUP_FIX.md](CART_CLEANUP_FIX.md#orphanremoval-vs-cascade--cascadetype-remove)

## 📝 Changelog

### v1.0 - Implementação Inicial
- ✅ Módulos: User, Product, Cart, Order
- ✅ Event-Driven Architecture
- ✅ Gerenciamento automático de estoque
- ✅ Checkout e cancelamento
- ✅ Documentação completa

## 🤝 Como Contribuir

1. Leia a documentação completa
2. Entenda a arquitetura
3. Siga os padrões estabelecidos
4. Atualize a documentação

## ❓ FAQ

**P: Onde encontro exemplos de uso?**  
R: [API_DOCUMENTATION.md - Exemplos de Uso](API_DOCUMENTATION.md#exemplos-de-uso)

**P: Como funciona o gerenciamento de estoque?**  
R: [API_DOCUMENTATION.md - Gerenciamento de Estoque](API_DOCUMENTATION.md#5-gerenciamento-de-estoque)

**P: Por que usar eventos?**  
R: [API_DOCUMENTATION.md - Eventos de Domínio](API_DOCUMENTATION.md#eventos-de-domínio)

**P: Como debugar problemas?**  
R: [API_DOCUMENTATION.md - Monitoramento](API_DOCUMENTATION.md#monitoramento)

---

**📚 Documentação Organizada do E-commerce Modular Monolith**

*Para começar, leia o [README.md](README.md) e depois a [API_DOCUMENTATION.md](API_DOCUMENTATION.md)*

