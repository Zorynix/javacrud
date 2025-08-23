package crudjava.crudjava.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import crudjava.crudjava.dto.CustomerDTO;
import crudjava.crudjava.model.Customer;
import crudjava.crudjava.service.CustomerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody Customer customer) {
        logger.info("Creating new customer with email: {}", customer.getEmail());
        Customer savedCustomer = customerService.createCustomer(customer);
        CustomerDTO customerDTO = new CustomerDTO(savedCustomer);
        logger.info("Successfully created customer with ID: {}", savedCustomer.getId());
        return new ResponseEntity<>(customerDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        logger.info("Fetching customer with ID: {}", id);
        return customerService.findById(id)
                .map(customer -> {
                    logger.info("Found customer: {}", customer.getEmail());
                    return ResponseEntity.ok(new CustomerDTO(customer));
                })
                .orElseGet(() -> {
                    logger.warn("Customer not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(Pageable pageable) {
        logger.info("Fetching all customers, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Customer> customers = customerService.findAll(pageable);
        Page<CustomerDTO> customerDTOs = customers.map(CustomerDTO::new);
        logger.info("Retrieved {} customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomersByName(
            @RequestParam String name, Pageable pageable) {
        String decodedName = crudjava.crudjava.util.UrlUtils.autoDecodeIfNeeded(name);
        logger.info("Searching customers by name: '{}' (decoded: '{}')", name, decodedName);
        Page<Customer> customers = customerService.findByNameContaining(decodedName, pageable);
        Page<CustomerDTO> customerDTOs = customers.map(CustomerDTO::new);
        logger.info("Found {} customers matching name search", customers.getTotalElements());
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        String decodedEmail = crudjava.crudjava.util.UrlUtils.autoDecodeIfNeeded(email);
        logger.info("Searching customer by email: '{}' (decoded: '{}')", email, decodedEmail);
        return customerService.findByEmail(decodedEmail)
                .map(customer -> {
                    logger.info("Found customer with email: {}", customer.getEmail());
                    return ResponseEntity.ok(new CustomerDTO(customer));
                })
                .orElseGet(() -> {
                    logger.warn("Customer not found with email: {}", decodedEmail);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<CustomerDTO>> getCustomersByType(
            @PathVariable String customerType) {
        String decodedType = crudjava.crudjava.util.UrlUtils.autoDecodeIfNeeded(customerType);
        logger.info("Searching customers by type: '{}' (decoded: '{}')", customerType, decodedType);
        
        List<Customer> customers = customerService.findByCustomerType(decodedType);
        List<CustomerDTO> customerDTOs = customers.stream().map(CustomerDTO::new).toList();
        logger.info("Found {} customers of type '{}'", customers.size(), decodedType);
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CustomerDTO>> getActiveCustomers(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(defaultValue = "1") long minOrders) {
        List<Customer> customers = customerService.findActiveCustomers(startDate, endDate, minOrders);
        List<CustomerDTO> customerDTOs = customers.stream().map(CustomerDTO::new).toList();
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<CustomerDTO>> getHighValueCustomers(
            @RequestParam BigDecimal minAmount,
            @RequestParam LocalDateTime since) {
        List<Customer> customers = customerService.findHighValueCustomers(minAmount, since);
        List<CustomerDTO> customerDTOs = customers.stream().map(CustomerDTO::new).toList();
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/by-spending")
    public ResponseEntity<List<CustomerDTO>> getCustomersByTotalSpending(
            @RequestParam BigDecimal totalSpent) {
        List<Customer> customers = customerService.findCustomersByTotalSpending(totalSpent);
        List<CustomerDTO> customerDTOs = customers.stream().map(CustomerDTO::new).toList();
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/stats/new-since")
    public ResponseEntity<Long> getNewCustomersCount(@RequestParam LocalDateTime date) {
        long count = customerService.countNewCustomersSince(date);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/order-count")
    public ResponseEntity<Long> getCustomerOrderCount(@PathVariable Long id) {
        long count = customerService.getCustomerOrderCount(id);
        return ResponseEntity.ok(count);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id, @Valid @RequestBody Customer customer) {
        logger.info("Updating customer with ID: {}", id);
        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        CustomerDTO customerDTO = new CustomerDTO(updatedCustomer);
        logger.info("Successfully updated customer with ID: {}", id);
        return ResponseEntity.ok(customerDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        logger.info("Deleting customer with ID: {}", id);
        customerService.deleteCustomer(id);
        logger.info("Successfully deleted customer with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
