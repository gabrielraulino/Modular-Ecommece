package com.modulith.ecommerce.product;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.event.OrderCancelledEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService implements ProductModuleAPI {
    private final ProductRepository repository;

    public ProductDTO getProduct(Long id) {
        return repository.findById(id).map(ProductDTO::fromEntity).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public List<ProductDTO> getAllProducts() {
        return repository.findAll().stream().map(ProductDTO::fromEntity).collect(Collectors.toList());
    }

    public ProductDTO saveProduct(CreateProductDTO productDTO) {
        Product product = new Product(
                null,
                productDTO.name(),
                productDTO.description(),
                productDTO.priceAmount(),
                productDTO.stock(),
                LocalDateTime.now(),
                null
        );
        return ProductDTO.fromEntity(repository.save(product));
    }

    public ProductDTO updateProduct(Long id, CreateProductDTO productDTO) {
        Product existingProduct = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Product updatedProduct = new Product(
                existingProduct.getId(),
                productDTO.name(),
                productDTO.description(),
                productDTO.priceAmount(),
                productDTO.stock(),
                existingProduct.getCreatedAt(),
                LocalDateTime.now()
        );
        return ProductDTO.fromEntity(repository.save(updatedProduct));
    }

    public void deleteProduct(Long id) {
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Product not found");
        }
    }

    public ProductDTO updateProductStock(Long id, int newStock) {
        validateStock(newStock);

        Product existingProduct = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Product updatedProduct = new Product(
                existingProduct.getId(),
                existingProduct.getName(),
                existingProduct.getDescription(),
                existingProduct.getPriceAmount(),
                newStock,
                existingProduct.getCreatedAt(),
                LocalDateTime.now()
        );
        return ProductDTO.fromEntity(repository.save(updatedProduct));
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }

    @Override
    public Optional<ProductDTO> findProductById(Long productId) {
        return repository.findById(productId).map(ProductDTO::fromEntity);
    }

    @Override
    public void validateProductExists(Long productId) {
        if (repository.findById(productId).isEmpty()) {
            throw new IllegalArgumentException("Product not found with id: " + productId);
        }
    }

    /**
     * Listener para decrementar estoque quando um checkout for realizado.
     * Este método é executado de forma assíncrona após o checkout ser concluído.
     *
     * @param event Evento de checkout contendo os itens comprados
     */
    @ApplicationModuleListener
    public void onCheckoutEvent(CheckoutEvent event) {
        log.info("Processando atualização de estoque para checkout. Carrinho: {}, Usuário: {}",
                event.cartId(), event.userId());

        try {
            for (CheckoutEvent.CheckoutItem item : event.items()) {
                Product product = repository.findById(item.productId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.productId()));

                int newStock = product.getStock() - item.quantity();

                updateProductStock(item.productId(), newStock);

                log.info("Estoque atualizado para produto {}: {} -> {} (Decrementado: {})",
                        product.getId(), product.getStock(), newStock, item.quantity());
            }

            log.info("Estoque atualizado com sucesso para {} produtos do carrinho {}",
                    event.items().size(), event.cartId());

        } catch (Exception e) {
            log.error("Erro ao atualizar estoque para checkout do carrinho: {}", event.cartId(), e);
            throw new RuntimeException("Falha ao atualizar estoque após checkout", e);
        }
    }

    /**
     * Listener para incrementar estoque quando um pedido for cancelado.
     * Este método restaura o estoque dos produtos cancelados.
     *
     * @param event Evento de cancelamento contendo os itens a serem restaurados
     */
    @ApplicationModuleListener
    public void onOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Processando restauração de estoque para pedido cancelado. Pedido: {}, Usuário: {}",
                event.orderId(), event.userId());

        try {
            for (OrderCancelledEvent.CancelledItem item : event.items()) {
                Product product = repository.findById(item.productId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.productId()));

                int newStock = product.getStock() + item.quantity();

                updateProductStock(item.productId(), newStock);

                log.info("Estoque restaurado para produto {}: {} -> {} (Incrementado: {})",
                        product.getId(), product.getStock(), newStock, item.quantity());
            }

            log.info("Estoque restaurado com sucesso para {} produtos do pedido {}",
                    event.items().size(), event.orderId());

        } catch (Exception e) {
            log.error("Erro ao restaurar estoque para pedido cancelado: {}", event.orderId(), e);
            throw new RuntimeException("Falha ao restaurar estoque após cancelamento", e);
        }
    }
}
