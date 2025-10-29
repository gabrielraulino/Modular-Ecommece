package com.modulith.ecommerce.product;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;

    @GetMapping("/{id}")
    public ProductDTO getProduct(@PathVariable Long id) {
        return service.getProduct(id);
    }

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return service.getAllProducts();
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
