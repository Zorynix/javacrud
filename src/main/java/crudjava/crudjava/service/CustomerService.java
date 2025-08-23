package crudjava.crudjava.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import crudjava.crudjava.dto.CreateCustomerRequestDTO;
import crudjava.crudjava.dto.CustomerDTO;
import crudjava.crudjava.exception.CustomerHasOrdersException;
import crudjava.crudjava.exception.CustomerNotFoundException;
import crudjava.crudjava.exception.DuplicateEmailException;
import crudjava.crudjava.model.Customer;
import crudjava.crudjava.repository.CustomerRepository;
import crudjava.crudjava.repository.OrderRepository;
import crudjava.crudjava.util.UrlUtils;

@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public CustomerDTO createCustomer(CreateCustomerRequestDTO request) {
        logger.info("Creating new customer with email: {}", request.getEmail());
        
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Attempt to create customer with existing email: {}", request.getEmail());
            throw new DuplicateEmailException("Customer with email " + request.getEmail() + " already exists");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCustomerType(request.getCustomerType() != null ? request.getCustomerType() : "REGULAR");

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Successfully created customer with ID: {}", savedCustomer.getId());
        
        return new CustomerDTO(savedCustomer);
    }

    public CustomerDTO updateCustomer(Long id, CreateCustomerRequestDTO request) {
        logger.info("Updating customer with ID: {}", id);
        
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));

        if (!existingCustomer.getEmail().equals(request.getEmail())) {
            if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
                logger.warn("Attempt to update customer with existing email: {}", request.getEmail());
                throw new DuplicateEmailException("Customer with email " + request.getEmail() + " already exists");
            }
        }

        existingCustomer.setFirstName(request.getFirstName());
        existingCustomer.setLastName(request.getLastName());
        existingCustomer.setEmail(request.getEmail());
        existingCustomer.setPhone(request.getPhone());
        if (request.getCustomerType() != null) {
            existingCustomer.setCustomerType(request.getCustomerType());
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        logger.info("Successfully updated customer with ID: {}", id);
        
        return new CustomerDTO(updatedCustomer);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findById(Long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    logger.info("Found customer with ID: {}", id);
                    return new CustomerDTO(customer);
                });
    }

    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findByEmail(String email) {
        String decodedEmail = UrlUtils.autoDecodeIfNeeded(email);
        logger.info("Searching customer by email: {}", decodedEmail);
        return customerRepository.findByEmail(decodedEmail)
                .map(customer -> {
                    logger.info("Found customer with email: {}", decodedEmail);
                    return new CustomerDTO(customer);
                });
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByNameContaining(String name, Pageable pageable) {
        String decodedName = UrlUtils.autoDecodeIfNeeded(name);
        logger.info("Searching customers by name: {}", decodedName);
        Page<Customer> customers = customerRepository.findByNameContainingIgnoreCase(decodedName, pageable);
        logger.info("Found {} customers matching name search", customers.getTotalElements());
        return customers.map(CustomerDTO::new);
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findByCustomerType(String customerType) {
        String decodedType = UrlUtils.autoDecodeIfNeeded(customerType);
        logger.info("Searching customers by type: {}", decodedType);
        List<Customer> customers = customerRepository.findByCustomerType(decodedType);
        logger.info("Found {} customers of type '{}'", customers.size(), decodedType);
        return customers.stream().map(CustomerDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findActiveCustomers(LocalDateTime startDate, LocalDateTime endDate, long minOrders) {
        logger.info("Finding active customers between {} and {} with min {} orders", startDate, endDate, minOrders);
        List<Customer> customers = customerRepository.findActiveCustomers(startDate, endDate, minOrders);
        return customers.stream().map(CustomerDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findHighValueCustomers(BigDecimal minAmount, LocalDateTime since) {
        logger.info("Finding high value customers with min amount {} since {}", minAmount, since);
        List<Customer> customers = customerRepository.findHighValueCustomers(minAmount, since);
        return customers.stream().map(CustomerDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findCustomersByTotalSpending(BigDecimal totalSpent) {
        logger.info("Finding customers by total spending: {}", totalSpent);
        List<Customer> customers = customerRepository.findCustomersByTotalSpending(totalSpent);
        return customers.stream().map(CustomerDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public long countNewCustomersSince(LocalDateTime date) {
        logger.info("Counting new customers since: {}", date);
        return customerRepository.countNewCustomersSince(date);
    }

    @Transactional(readOnly = true)
    public long getCustomerOrderCount(Long customerId) {
        logger.info("Getting order count for customer: {}", customerId);
        return orderRepository.countOrdersByCustomer(customerId);
    }

    public void deleteCustomer(Long id) {
        logger.info("Deleting customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));

        long orderCount = getCustomerOrderCount(id);
        if (orderCount > 0) {
            logger.warn("Cannot delete customer {} with {} existing orders", id, orderCount);
            throw new CustomerHasOrdersException("Cannot delete customer with existing orders");
        }

        customerRepository.delete(customer);
        logger.info("Successfully deleted customer: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> findAll(Pageable pageable) {
        logger.info("Fetching all customers, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Customer> customers = customerRepository.findAll(pageable);
        logger.info("Retrieved {} customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());
        return customers.map(CustomerDTO::new);
    }
}
