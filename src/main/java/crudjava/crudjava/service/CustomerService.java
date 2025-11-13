package crudjava.crudjava.service;

import crudjava.crudjava.dto.CreateCustomerRequestDTO;
import crudjava.crudjava.dto.CustomerDTO;
import crudjava.crudjava.dto.UpdateCustomerRequestDTO;
import crudjava.crudjava.exception.CustomerHasOrdersException;
import crudjava.crudjava.exception.CustomerNotFoundException;
import crudjava.crudjava.exception.DuplicateEmailException;
import crudjava.crudjava.mapper.CustomerMapper;
import crudjava.crudjava.model.Customer;
import crudjava.crudjava.repository.CustomerRepository;
import crudjava.crudjava.repository.OrderRepository;
import crudjava.crudjava.util.UrlUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerDTO createCustomer(CreateCustomerRequestDTO request) {
        log.info("Creating new customer with email: {}", request.getEmail());

        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn(
                "Attempt to create customer with existing email: {}",
                request.getEmail()
            );
            throw new DuplicateEmailException(
                "Customer with email " + request.getEmail() + " already exists"
            );
        }

        Customer customer = Customer.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .customerType(
                request.getCustomerType() != null
                    ? request.getCustomerType()
                    : "REGULAR"
            )
            .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info(
            "Successfully created customer with ID: {}",
            savedCustomer.getId()
        );

        return CustomerMapper.toDTO(savedCustomer);
    }

    public CustomerDTO updateCustomer(
        Long id,
        UpdateCustomerRequestDTO request
    ) {
        log.info("Updating customer with ID: {}", id);

        Customer existingCustomer = customerRepository
            .findById(id)
            .orElseThrow(() ->
                new CustomerNotFoundException("Customer not found: " + id)
            );

        // Check email uniqueness only if email is being updated
        if (
            request.getEmail() != null &&
            !existingCustomer.getEmail().equals(request.getEmail())
        ) {
            if (
                customerRepository.findByEmail(request.getEmail()).isPresent()
            ) {
                log.warn(
                    "Attempt to update customer with existing email: {}",
                    request.getEmail()
                );
                throw new DuplicateEmailException(
                    "Customer with email " +
                        request.getEmail() +
                        " already exists"
                );
            }
        }

        // Update only provided fields
        if (request.getFirstName() != null) {
            existingCustomer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingCustomer.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            existingCustomer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            existingCustomer.setPhone(request.getPhone());
        }
        if (request.getCustomerType() != null) {
            existingCustomer.setCustomerType(request.getCustomerType());
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Successfully updated customer with ID: {}", id);

        return CustomerMapper.toDTO(updatedCustomer);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findById(Long id) {
        return customerRepository
            .findById(id)
            .map(customer -> {
                log.info("Found customer with ID: {}", id);
                return CustomerMapper.toDTO(customer);
            });
    }

    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findByEmail(String email) {
        String decodedEmail = UrlUtils.autoDecodeIfNeeded(email);
        log.info("Searching customer by email: {}", decodedEmail);
        return customerRepository
            .findByEmail(decodedEmail)
            .map(customer -> {
                log.info("Found customer with email: {}", decodedEmail);
                return CustomerMapper.toDTO(customer);
            });
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByNameContaining(
        String name,
        Pageable pageable
    ) {
        String decodedName = UrlUtils.autoDecodeIfNeeded(name);
        log.info("Searching customers by name: {}", decodedName);
        Page<Customer> customers =
            customerRepository.findByNameContainingIgnoreCase(
                decodedName,
                pageable
            );
        log.info(
            "Found {} customers matching name search",
            customers.getTotalElements()
        );
        return customers.map(CustomerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findByCustomerType(String customerType) {
        String decodedType = UrlUtils.autoDecodeIfNeeded(customerType);
        log.info("Searching customers by type: {}", decodedType);
        List<Customer> customers = customerRepository.findByCustomerType(
            decodedType
        );
        log.info(
            "Found {} customers of type '{}'",
            customers.size(),
            decodedType
        );
        return customers.stream().map(CustomerMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findActiveCustomers(
        LocalDateTime startDate,
        LocalDateTime endDate,
        long minOrders
    ) {
        log.info(
            "Finding active customers between {} and {} with min {} orders",
            startDate,
            endDate,
            minOrders
        );
        List<Customer> customers = customerRepository.findActiveCustomers(
            startDate,
            endDate,
            minOrders
        );
        return customers.stream().map(CustomerMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findHighValueCustomers(
        BigDecimal minAmount,
        LocalDateTime since
    ) {
        log.info(
            "Finding high value customers with min amount {} since {}",
            minAmount,
            since
        );
        List<Customer> customers = customerRepository.findHighValueCustomers(
            minAmount,
            since
        );
        return customers.stream().map(CustomerMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findCustomersByTotalSpending(
        BigDecimal totalSpent
    ) {
        log.info("Finding customers by total spending: {}", totalSpent);
        List<Customer> customers =
            customerRepository.findCustomersByTotalSpending(totalSpent);
        return customers.stream().map(CustomerMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public long countNewCustomersSince(LocalDateTime date) {
        log.info("Counting new customers since: {}", date);
        return customerRepository.countNewCustomersSince(date);
    }

    @Transactional(readOnly = true)
    public long getCustomerOrderCount(Long customerId) {
        log.info("Getting order count for customer: {}", customerId);
        return orderRepository.countOrdersByCustomer(customerId);
    }

    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);
        Customer customer = customerRepository
            .findById(id)
            .orElseThrow(() ->
                new CustomerNotFoundException("Customer not found: " + id)
            );

        long orderCount = getCustomerOrderCount(id);
        if (orderCount > 0) {
            log.warn(
                "Cannot delete customer {} with {} existing orders",
                id,
                orderCount
            );
            throw new CustomerHasOrdersException(
                "Cannot delete customer with existing orders"
            );
        }

        customerRepository.delete(customer);
        log.info("Successfully deleted customer: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> findAll(Pageable pageable) {
        log.info(
            "Fetching all customers, page: {}, size: {}",
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        Page<Customer> customers = customerRepository.findAll(pageable);
        log.info(
            "Retrieved {} customers out of {} total",
            customers.getNumberOfElements(),
            customers.getTotalElements()
        );
        return customers.map(CustomerMapper::toDTO);
    }
}
