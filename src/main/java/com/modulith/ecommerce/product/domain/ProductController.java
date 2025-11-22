package com.modulith.ecommerce.product.domain;

import com.modulith.ecommerce.product.CreateProductDTO;
import com.modulith.ecommerce.product.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/products")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    private final ProductService service;

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ProductDTO getProduct(@PathVariable Long id) {
        return service.getProduct(id);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public List<ProductDTO> getAllProducts(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.getAllProducts(pageable);
    }

    @PostMapping
    public ProductDTO createProduct(@RequestBody CreateProductDTO productDTO) {
        return service.saveProduct(productDTO);
    }

    @PutMapping("/{id}")
    public ProductDTO updateProduct(@PathVariable Long id, CreateProductDTO productDTO) {
        return service.updateProduct(id, productDTO);
    }
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
    }

    @PatchMapping("/{id}/stock")
    public ProductDTO updateProductStock(@PathVariable Long id, @RequestParam int stock) {
        return service.updateProductStock(id, stock);
    }
}
