package com.warehouse.product_tracker.controller;

import com.warehouse.product_tracker.exception.InsufficientStockException;
import com.warehouse.product_tracker.model.Product;
import com.warehouse.product_tracker.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController // Marks this as a REST controller
@RequestMapping("/api/products") // Base path for all methods
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // --- 1. CRUD: Create (POST /api/products) ---
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product newProduct = productService.save(product);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED); // Returns 201 Created
    }

    // --- 2. CRUD: Read All (GET /api/products) ---
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    // --- 3. CRUD: Read Single (GET /api/products/{id}) ---
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.findById(id)
            .map(ResponseEntity::ok) // Returns 200 OK if found
            .orElseGet(() -> ResponseEntity.notFound().build()); // Returns 404 Not Found
    }

    // --- 4. CRUD: Update (PUT /api/products/{id}) ---
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productService.findById(id)
            .map(product -> {
                // Update only the fields provided, stock is handled by separate endpoints
                product.setName(productDetails.getName());
                product.setDescription(productDetails.getDescription());
                product.setLowStockThreshold(productDetails.getLowStockThreshold());
                // NOTE: stockQuantity is generally NOT updated via PUT/PATCH to enforce business rules
                // But for full CRUD, we allow it, trusting the service layer's negative check.
                product.setStockQuantity(productDetails.getStockQuantity());

                Product updatedProduct = productService.save(product);
                return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- 5. CRUD: Delete (DELETE /api/products/{id}) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.findById(id).isPresent()) {
            productService.deleteById(id);
            return ResponseEntity.noContent().build(); // Returns 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // Returns 404 Not Found
        }
    }

    // --- Inventory Logic: Increase Stock (POST /api/products/{id}/increase-stock) ---
    @PostMapping("/{id}/increasestock")
    public ResponseEntity<Product> increaseStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            Product updatedProduct = productService.increaseStock(id, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().build(); // Returns 400 Bad Request
        }
    }

    // --- Inventory Logic: Decrease Stock (POST /api/products/{id}/decrease-stock) ---
    @PostMapping("/{id}/decreasestock")
    public ResponseEntity<Product> decreaseStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            Product updatedProduct = productService.decreaseStock(id, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (InsufficientStockException e) {
            // Handled by @ResponseStatus in the exception class, but we catch it for clarity
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Returns 400 Bad Request
        }
    }

    // --- Bonus Feature: Low Stock List (GET /api/products/low-stock) ---
    @GetMapping("/lowstock")
    public List<Product> getLowStockProducts() {
        return productService.getLowStockProducts();
    }
}