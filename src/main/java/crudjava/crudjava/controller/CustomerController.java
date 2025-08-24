package crudjava.crudjava.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

import crudjava.crudjava.dto.CreateCustomerRequestDTO;
import crudjava.crudjava.dto.CustomerDTO;
import crudjava.crudjava.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerRequestDTO request) {
        CustomerDTO customerDTO = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(Pageable pageable) {
        Page<CustomerDTO> customers = customerService.findAll(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomersByName(
            @RequestParam String name, Pageable pageable) {
        Page<CustomerDTO> customers = customerService.findByNameContaining(name, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        return customerService.findByEmail(email)
                .map(customerDTO -> ResponseEntity.ok(customerDTO))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<CustomerDTO>> getCustomersByType(
            @PathVariable String customerType) {
        List<CustomerDTO> customers = customerService.findByCustomerType(customerType);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CustomerDTO>> getActiveCustomers(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(defaultValue = "1") long minOrders) {
        List<CustomerDTO> customers = customerService.findActiveCustomers(startDate, endDate, minOrders);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<CustomerDTO>> getHighValueCustomers(
            @RequestParam BigDecimal minAmount,
            @RequestParam LocalDateTime since) {
        List<CustomerDTO> customers = customerService.findHighValueCustomers(minAmount, since);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/by-spending")
    public ResponseEntity<List<CustomerDTO>> getCustomersByTotalSpending(
            @RequestParam BigDecimal totalSpent) {
        List<CustomerDTO> customers = customerService.findCustomersByTotalSpending(totalSpent);
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
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id, @Valid @RequestBody CreateCustomerRequestDTO request) {
        CustomerDTO customerDTO = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(customerDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
