package com.modulith.ecommerce.product.domain;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.event.OrderCancelledEvent;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.exception.ValidationException;
import com.modulith.ecommerce.exception.InsufficientStockException;
import com.modulith.ecommerce.product.CreateProductDTO;
import com.modulith.ecommerce.product.ProductDTO;
import com.modulith.ecommerce.product.ProductModuleAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<ProductDTO> getAllProducts(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ProductDTO::fromEntity)
                .getContent();
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
        repository.delete(getProductById(id));
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


    private Product updateProductStock(Product product, int newStock) {
        validateStock(newStock);

        Product updatedProduct = new Product(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPriceAmount(),
                newStock,
                product.getCreatedAt(),
                LocalDateTime.now()
        );
        return repository.save(updatedProduct);
    }


    @Override
    public List<ProductDTO> findAllProductsByIds(Set<Long> productIds) {
        return repository.findAllByIdIn(productIds).stream()
                .map(ProductDTO::fromEntity)
                .toList();
    }

    @Override
    public void validateProductStock(Long productId, int requiredQuantity) {
        Optional<Product> product = repository.findById(productId);

        if (product.isEmpty()) {
            throw new ResourceNotFoundException("Product", productId);
        }

        if (product.get().getStock() < requiredQuantity) {
            throw new InsufficientStockException(product.get().getName(), requiredQuantity, product.get().getStock());
        }

    }

    @Override
    public void validateProductsStock(Map<Long, Integer> productQuantities) {
        // receive to map of productId -> requiredQuantity to avoid multiple DB calls (n+1 problem)

        List<Product> products = repository.findAllByIdIn(productQuantities.keySet());

        // fails in verify stock to avoid partial updates
        products.forEach(product -> {
            int requiredQuantity = productQuantities.get(product.getId());
            if (product.getStock() < requiredQuantity) {
                log.error("Product {} has stock less than required quantity", product.getId());
                throw new InsufficientStockException(product.getName(), requiredQuantity, product.getStock());
            }
        });

    }

    @EventListener
    public void onCheckoutEvent(CheckoutEvent event) {
        log.info("Processing stock update for checkout. Cart: {}, User: {}",
                event.cart(), event.user());

        Map<Long, Integer> productQuantities = event.items().stream()
                .collect(Collectors.toMap(
                        CheckoutEvent.CheckoutItem::product,
                        CheckoutEvent.CheckoutItem::quantity
                ));

        List<Product> products = repository.findAllByIdIn(productQuantities.keySet());

        products.forEach(product -> {
            int requiredQuantity = productQuantities.get(product.getId());
            if (product.getStock() < requiredQuantity) {
                throw new InsufficientStockException(product.getName(), requiredQuantity, product.getStock());
            }
        });

        List<Product> updatedProducts = products.stream().map(product -> {
            int requiredQuantity = productQuantities.get(product.getId());
            int newStock = product.getStock() - requiredQuantity;
            log.info("Stock updated for product {}: {} -> {} (Decremented by: {})",
                    product.getId(), product.getStock(), newStock, requiredQuantity);
            return updateProductStock(product, newStock);
        }).toList();

        log.info("Stock updated successfully for {} products in cart {}",
                productQuantities.size(), event.cart());
        repository.saveAll(updatedProducts);

    }

    @ApplicationModuleListener
    public void onOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Processing stock restoration for cancelled order. Order: {}, User: {}",
                event.orderId(), event.userId());

        Set<Long> productIds = event.items().stream().map(OrderCancelledEvent.CancelledItem::productId).collect(Collectors.toSet());

        List<Product> products = repository.findAllByIdIn(productIds);

        for (OrderCancelledEvent.CancelledItem item : event.items()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(item.productId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));

            int oldStock = product.getStock();
            int newStock = product.getStock() + item.quantity();

            updateProductStock(product, newStock);

            log.info("Stock restored for product {}: {} -> {} (Incremented by: {})",
                    product.getId(), oldStock, newStock, item.quantity());
        }

        log.info("Stock restored successfully for {} products in order {}",
                event.items().size(), event.orderId());

    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new ValidationException("stock", String.valueOf(stock), "cannot be negative");
        }
    }

    private Product getProductById(Long product) {
        return repository.findById(product)
                .orElseThrow(() -> new ResourceNotFoundException("Product", product));
    }
}
