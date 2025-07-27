package crudjava.crudjava.repository;

import crudjava.crudjava.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = :status")
    Page<Order> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                        @Param("status") String status,
                                        Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount AND o.status IN :statuses")
    List<Order> findHighValueOrders(@Param("minAmount") BigDecimal minAmount,
                                  @Param("statuses") List<String> statuses);

    @Query(value = """
        SELECT DATE(o.created_at) as order_date, 
               COUNT(*) as order_count, 
               SUM(o.total_amount) as total_revenue 
        FROM orders o 
        WHERE o.created_at >= :startDate 
        GROUP BY DATE(o.created_at) 
        ORDER BY order_date DESC
        """, nativeQuery = true)
    List<Object[]> getDailySalesReport(@Param("startDate") LocalDateTime startDate);

    @Query(value = """
        SELECT o.status, COUNT(*) as count, AVG(o.total_amount) as avg_amount 
        FROM orders o 
        WHERE o.created_at >= :startDate 
        GROUP BY o.status
        """, nativeQuery = true)
    List<Object[]> getOrderStatusStatistics(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.deliveredAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByPeriod(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT p.name, SUM(oi.quantity) as total_quantity, SUM(oi.subtotal) as total_revenue
        FROM orders o 
        JOIN order_items oi ON o.id = oi.order_id 
        JOIN products p ON oi.product_id = p.id 
        WHERE o.status IN ('DELIVERED', 'SHIPPED') 
        AND o.created_at >= :startDate 
        GROUP BY p.id, p.name 
        ORDER BY total_revenue DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getTopSellingProducts(@Param("startDate") LocalDateTime startDate,
                                       @Param("limit") int limit);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId AND o.status != 'CANCELLED'")
    long countOrdersByCustomer(@Param("customerId") Long customerId);
}
