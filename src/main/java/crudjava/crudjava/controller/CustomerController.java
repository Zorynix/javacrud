package crudjava.crudjava.controller;

import crudjava.crudjava.dto.CustomerDTO;
import crudjava.crudjava.model.Customer;
import crudjava.crudjava.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            Customer savedCustomer = customerService.createCustomer(customer);
            CustomerDTO customerDTO = new CustomerDTO(savedCustomer);
            return new ResponseEntity<>(customerDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(new CustomerDTO(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerService.findAll(pageable);
        Page<CustomerDTO> customerDTOs = customers.map(CustomerDTO::new);
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomersByName(
            @RequestParam String name, Pageable pageable) {
        Page<Customer> customers = customerService.findByNameContaining(name, pageable);
        Page<CustomerDTO> customerDTOs = customers.map(CustomerDTO::new);
        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        return customerService.findByEmail(email)
                .map(customer -> ResponseEntity.ok(new CustomerDTO(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<CustomerDTO>> getCustomersByType(
            @PathVariable String customerType) {
        List<Customer> customers = customerService.findByCustomerType(customerType);
        List<CustomerDTO> customerDTOs = customers.stream().map(CustomerDTO::new).toList();
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
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customer);
            CustomerDTO customerDTO = new CustomerDTO(updatedCustomer);
            return ResponseEntity.ok(customerDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
