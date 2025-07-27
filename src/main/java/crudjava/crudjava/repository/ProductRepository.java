package crudjava.crudjava.repository;

import crudjava.crudjava.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(String category);

    List<Product> findByStatus(Product.ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.name ILIKE %:name% AND p.status = 'ACTIVE'")
    Page<Product> findByNameContainingAndActive(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.price BETWEEN :minPrice AND :maxPrice AND p.status = 'ACTIVE'")
    Page<Product> findByCategoryAndPriceRange(@Param("category") String category,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice,
                                            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.status = 'ACTIVE'")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query(value = """
        SELECT p.*, COALESCE(SUM(oi.quantity), 0) as total_sold 
        FROM products p 
        LEFT JOIN order_items oi ON p.id = oi.product_id 
        LEFT JOIN orders o ON oi.order_id = o.id 
        WHERE o.status IN ('DELIVERED', 'SHIPPED') 
        GROUP BY p.id 
        ORDER BY total_sold DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findBestSellingProducts(@Param("limit") int limit);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :productId")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.category")
    List<String> findAllActiveCategories();

    @Query(value = """
        SELECT AVG(p.price) as avg_price, p.category 
        FROM products p 
        WHERE p.status = 'ACTIVE' 
        GROUP BY p.category
        """, nativeQuery = true)
    List<Object[]> findAveragePriceByCategory();
}
