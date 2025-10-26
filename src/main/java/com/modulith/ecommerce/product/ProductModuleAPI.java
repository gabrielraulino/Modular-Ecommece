package com.modulith.ecommerce.product;

import java.util.Optional;

public interface ProductModuleAPI {

    Optional<ProductDTO> findProductById(Long productId);

    ProductDTO validateProductStock(Long productId, int requiredQuantity);
}
