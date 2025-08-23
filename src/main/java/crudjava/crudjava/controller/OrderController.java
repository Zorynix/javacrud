package crudjava.crudjava.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import crudjava.crudjava.dto.CreateOrderRequestDTO;
import crudjava.crudjava.dto.OrderDTO;
import crudjava.crudjava.service.OrderService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        OrderDTO orderDTO = orderService.createOrder(request);
        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(Pageable pageable) {
        Page<OrderDTO> orders = orderService.findAll(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return orderService.findById(id)
                .map(orderDTO -> ResponseEntity.ok(orderDTO))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(orderDTO -> ResponseEntity.ok(orderDTO))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByCustomer(
            @PathVariable Long customerId, Pageable pageable) {
        Page<OrderDTO> orders = orderService.findOrdersByCustomer(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<OrderDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<OrderDTO> orders = orderService.findOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<OrderDTO>> getHighValueOrders(@RequestParam BigDecimal minAmount) {
        List<OrderDTO> orders = orderService.findHighValueOrders(minAmount);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/reports/daily-sales")
    public ResponseEntity<List<Object[]>> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        List<Object[]> report = orderService.getDailySalesReport(startDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal revenue = orderService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id, @RequestParam String status) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }
}
