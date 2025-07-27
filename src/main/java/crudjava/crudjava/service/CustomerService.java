package crudjava.crudjava.service;

import crudjava.crudjava.model.Customer;
import crudjava.crudjava.repository.CustomerRepository;
import crudjava.crudjava.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Customer createCustomer(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Customer with email " + customer.getEmail() + " already exists");
        }

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Created new customer: {} {}", savedCustomer.getFirstName(), savedCustomer.getLastName());
        return savedCustomer;
    }

    public Customer updateCustomer(Long id, Customer customerUpdates) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        if (!existingCustomer.getEmail().equals(customerUpdates.getEmail())) {
            if (customerRepository.findByEmail(customerUpdates.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Customer with email " + customerUpdates.getEmail() + " already exists");
            }
        }

        existingCustomer.setFirstName(customerUpdates.getFirstName());
        existingCustomer.setLastName(customerUpdates.getLastName());
        existingCustomer.setEmail(customerUpdates.getEmail());
        existingCustomer.setPhone(customerUpdates.getPhone());
        existingCustomer.setCustomerType(customerUpdates.getCustomerType());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        logger.info("Updated customer: {}", updatedCustomer.getId());
        return updatedCustomer;
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<Customer> findByNameContaining(String name, Pageable pageable) {
        return customerRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> findByCustomerType(String customerType) {
        return customerRepository.findByCustomerType(customerType);
    }

    @Transactional(readOnly = true)
    public List<Customer> findActiveCustomers(LocalDateTime startDate, LocalDateTime endDate, long minOrders) {
        return customerRepository.findActiveCustomers(startDate, endDate, minOrders);
    }

    @Transactional(readOnly = true)
    public List<Customer> findHighValueCustomers(BigDecimal minAmount, LocalDateTime since) {
        return customerRepository.findHighValueCustomers(minAmount, since);
    }

    @Transactional(readOnly = true)
    public List<Customer> findCustomersByTotalSpending(BigDecimal totalSpent) {
        return customerRepository.findCustomersByTotalSpending(totalSpent);
    }

    @Transactional(readOnly = true)
    public long countNewCustomersSince(LocalDateTime date) {
        return customerRepository.countNewCustomersSince(date);
    }

    @Transactional(readOnly = true)
    public long getCustomerOrderCount(Long customerId) {
        return orderRepository.countOrdersByCustomer(customerId);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        long orderCount = getCustomerOrderCount(id);
        if (orderCount > 0) {
            throw new IllegalStateException("Cannot delete customer with existing orders");
        }

        customerRepository.delete(customer);
        logger.info("Deleted customer: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
}
