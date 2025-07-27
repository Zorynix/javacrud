package crudjava.crudjava.repository;

import crudjava.crudjava.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    List<Customer> findByCustomerType(String customerType);

    @Query("SELECT c FROM Customer c WHERE c.firstName ILIKE %:name% OR c.lastName ILIKE %:name%")
    Page<Customer> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT c FROM Customer c JOIN c.orders o WHERE o.createdAt BETWEEN :startDate AND :endDate GROUP BY c HAVING COUNT(o) >= :minOrders")
    List<Customer> findActiveCustomers(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("minOrders") long minOrders);

    @Query("SELECT c FROM Customer c WHERE c.id IN " +
           "(SELECT DISTINCT o.customer.id FROM Order o WHERE o.status = 'DELIVERED' " +
           "AND o.totalAmount >= :minAmount AND o.createdAt >= :since)")
    List<Customer> findHighValueCustomers(@Param("minAmount") java.math.BigDecimal minAmount,
                                        @Param("since") LocalDateTime since);

    @Query(value = """
        SELECT c.* FROM customers c 
        WHERE c.id IN (
            SELECT o.customer_id FROM orders o 
            GROUP BY o.customer_id 
            HAVING SUM(o.total_amount) >= :totalSpent
        )
        """, nativeQuery = true)
    List<Customer> findCustomersByTotalSpending(@Param("totalSpent") java.math.BigDecimal totalSpent);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :date")
    long countNewCustomersSince(@Param("date") LocalDateTime date);
}
