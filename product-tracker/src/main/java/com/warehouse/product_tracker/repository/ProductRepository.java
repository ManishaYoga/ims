package com.warehouse.product_tracker.repository;

import com.warehouse.product_tracker.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- NEW IMPORT
import java.util.List;

// JpaRepository gives us all basic CRUD methods for free!
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Removed the problematic findByStockQuantityLessThan(int stockQuantity) as it was redundant/unclear.
    // The previous error came from the second method, which we are now replacing with JPQL.

    // Custom method for Bonus Feature: Find all products below their OWN lowStockThreshold
    // FIX: Use @Query to directly compare two columns (p.stockQuantity < p.lowStockThreshold)
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < p.lowStockThreshold")
    List<Product> findLowStockProducts(); // Renamed for clarity, though not strictly required
}