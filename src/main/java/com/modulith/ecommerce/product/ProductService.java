package com.modulith.ecommerce.product;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.event.OrderCancelledEvent;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.exception.ValidationException;
import com.modulith.ecommerce.exception.InsufficientStockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService implements ProductModuleAPI {
    private final ProductRepository repository;

    public ProductDTO getProduct(Long id) {
        return repository.findById(id)
                .map(ProductDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public List<ProductDTO> getAllProducts() {
        return repository.findAll().stream().map(ProductDTO::fromEntity).toList();
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
        Product existingProduct = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
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

        Product existingProduct = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

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


    @Override
    public Optional<ProductDTO> findProductById(Long productId) {
        return repository.findById(productId).map(ProductDTO::fromEntity);
    }

    @Override
    public ProductDTO validateProductStock(Long productId, int requiredQuantity) {
        Optional<Product> product = repository.findById(productId);

        if (product.isEmpty()) {
            throw new ResourceNotFoundException("Product", productId);
        }

        if (product.get().getStock() < requiredQuantity) {
            throw new InsufficientStockException(product.get().getName(), requiredQuantity, product.get().getStock());
        }

        return product.map(ProductDTO::fromEntity).get();
    }


    @TransactionalEventListener
    public void onCheckoutEvent(CheckoutEvent event) {
        log.info("Processing stock update for checkout. Cart: {}, User: {}",
                event.cartId(), event.userId());

        try {

            for (CheckoutEvent.CheckoutItem item : event.items()) {
                Product product = repository.findById(item.productId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));

                int oldStock = product.getStock();
                int newStock = product.getStock() - item.quantity();

                updateProductStock(item.productId(), newStock);

                log.info("Stock updated for product {}: {} -> {} (Decremented by: {})",
                        product.getId(), oldStock, newStock, item.quantity());
            }

            log.info("Stock updated successfully for {} products in cart {}",
                    event.items().size(), event.cartId());

        } catch (Exception e) {
            log.error("Error updating stock for checkout cart: {}", event.cartId(), e);
            // This will cause the entire transaction to rollback
            throw new RuntimeException("Failed to update stock after checkout", e);
        }
    }

    @ApplicationModuleListener
    public void onOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Processing stock restoration for cancelled order. Order: {}, User: {}",
                event.orderId(), event.userId());

        try {
            for (OrderCancelledEvent.CancelledItem item : event.items()) {
                Product product = repository.findById(item.productId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));

                int oldStock = product.getStock();
                int newStock = product.getStock() + item.quantity();

                updateProductStock(item.productId(), newStock);

                log.info("Stock restored for product {}: {} -> {} (Incremented by: {})",
                        product.getId(), oldStock, newStock, item.quantity());
            }

            log.info("Stock restored successfully for {} products in order {}",
                    event.items().size(), event.orderId());

        } catch (Exception e) {
            log.error("Error restoring stock for cancelled order: {}", event.orderId(), e);
            throw new RuntimeException("Failed to restore stock after cancellation", e);
        }
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new ValidationException("stock", String.valueOf(stock), "cannot be negative");
        }
    }

}
