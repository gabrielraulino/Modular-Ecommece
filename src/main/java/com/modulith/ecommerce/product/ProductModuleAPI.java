package com.modulith.ecommerce.product;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductModuleAPI {

    /**
     * Find all products by their IDs
     * @param productIds Set of product IDs
     * @return List of ProductDTOs
     */
    List<ProductDTO> findAllProductsByIds(Set<Long> productIds);
    
    /**
     * Validate product stock
     * @param productId Product ID
     * @param requiredQuantity Required quantity
     */
    void validateProductStock(Long productId, int requiredQuantity);

    /**
     * Validate products stock
     * @param productQuantities Map of product IDs and required quantities
     */
    void validateProductsStock(Map<Long, Integer> productQuantities);
}
