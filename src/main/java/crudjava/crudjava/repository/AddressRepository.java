package crudjava.crudjava.repository;

import crudjava.crudjava.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerId(Long customerId);

    List<Address> findByCustomerIdAndAddressType(Long customerId, Address.AddressType addressType);

    List<Address> findByCity(String city);

    List<Address> findByCountry(String country);

    @Query("SELECT a FROM Address a WHERE a.customer.id = :customerId AND a.addressType = 'SHIPPING'")
    List<Address> findShippingAddressesByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT a FROM Address a WHERE a.customer.id = :customerId AND a.addressType = 'BILLING'")
    List<Address> findBillingAddressesByCustomer(@Param("customerId") Long customerId);

    @Query(value = """
        SELECT a.country, COUNT(*) as address_count 
        FROM addresses a 
        GROUP BY a.country 
        ORDER BY address_count DESC
        """, nativeQuery = true)
    List<Object[]> getAddressCountByCountry();
}
