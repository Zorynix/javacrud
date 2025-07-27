package crudjava.crudjava.controller;

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
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer savedCustomer = customerService.createCustomer(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(customer))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<Customer>> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerService.findAll(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Customer>> searchCustomersByName(
            @RequestParam String name, Pageable pageable) {
        Page<Customer> customers = customerService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        return customerService.findByEmail(email)
                .map(customer -> ResponseEntity.ok(customer))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<Customer>> getCustomersByType(
            @PathVariable Customer.CustomerType customerType) {
        List<Customer> customers = customerService.findByCustomerType(customerType);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Customer>> getActiveCustomers(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(defaultValue = "1") long minOrders) {
        List<Customer> customers = customerService.findActiveCustomers(startDate, endDate, minOrders);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<Customer>> getHighValueCustomers(
            @RequestParam BigDecimal minAmount,
            @RequestParam LocalDateTime since) {
        List<Customer> customers = customerService.findHighValueCustomers(minAmount, since);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/by-spending")
    public ResponseEntity<List<Customer>> getCustomersByTotalSpending(
            @RequestParam BigDecimal totalSpent) {
        List<Customer> customers = customerService.findCustomersByTotalSpending(totalSpent);
        return ResponseEntity.ok(customers);
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

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id, @Valid @RequestBody Customer customer) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customer);
            return ResponseEntity.ok(updatedCustomer);
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
