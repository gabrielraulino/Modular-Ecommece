package com.modulith.ecommerce.product;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ProductModuleAPI {

    Optional<ProductDTO> findProductById(Long productId);

    List<ProductDTO> findAllProductsByIds(Set<Long> productIds);

    ProductDTO validateProductStock(Long productId, int requiredQuantity);

    void validateProductsStock(Map<Long, Integer> productQuantities);
}
