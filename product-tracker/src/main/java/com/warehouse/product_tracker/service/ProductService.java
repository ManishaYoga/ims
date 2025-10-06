package com.warehouse.product_tracker.service;

import com.warehouse.product_tracker.exception.InsufficientStockException;
import com.warehouse.product_tracker.model.Product;
import com.warehouse.product_tracker.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service // Marks this class as a Spring Service component
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired // Spring injects the Repository instance here
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // --- Core Feature: Full CRUD ---

    public Product save(Product product) {
        // Core Logic: Ensure stock is not negative upon creation/update
        if (product.getStockQuantity() < 0) {
             throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        return productRepository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    // --- Inventory Logic: Increase Stock ---

    public Product increaseStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive.");
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        // Logic: Add stock
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productRepository.save(product);
    }

    // --- Inventory Logic: Decrease Stock (CRITICAL LOGIC) ---

    public Product decreaseStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive.");
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        // Core Inventory Logic: Check for insufficient stock (cannot go below zero)
        if (product.getStockQuantity() < quantity) {
            // Robust Error Handling: Throw our custom 400 exception
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                    id, product.getStockQuantity(), quantity));
        }

        // Logic: Remove stock
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productRepository.save(product);
    }

    // --- Bonus Feature: Low Stock List ---

    public List<Product> getLowStockProducts() {
        // Uses the custom repository method we defined
    	return productRepository.findLowStockProducts();
    }
}