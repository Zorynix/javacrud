package crudjava.crudjava.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import crudjava.crudjava.model.Order;
import crudjava.crudjava.service.OrderService;
import crudjava.crudjava.util.UrlUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        logger.info("Creating new order for customer ID: {}", request.getCustomerId());
        Order order = orderService.createOrder(request.getCustomerId(), request.getItems());
        logger.info("Successfully created order with ID: {} and number: {}", order.getId(), order.getOrderNumber());
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderService.findById(id)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        String decodedOrderNumber = UrlUtils.autoDecodeIfNeeded(orderNumber);
        logger.info("Searching order by number: '{}' (decoded: '{}')", orderNumber, decodedOrderNumber);
        return orderService.findByOrderNumber(decodedOrderNumber)
                .map(order -> {
                    logger.info("Found order with number: {}", order.getOrderNumber());
                    return ResponseEntity.ok(order);
                })
                .orElseGet(() -> {
                    logger.warn("Order not found with number: {}", decodedOrderNumber);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<Order>> getOrdersByCustomer(
            @PathVariable Long customerId, Pageable pageable) {
        Page<Order> orders = orderService.findOrdersByCustomer(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<Order>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<Order> orders = orderService.findOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<Order>> getHighValueOrders(@RequestParam BigDecimal minAmount) {
        List<Order> orders = orderService.findHighValueOrders(minAmount);
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
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id, @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public static class CreateOrderRequest {
        private Long customerId;
        private List<OrderService.OrderItemRequest> items;

        public CreateOrderRequest() {}

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public List<OrderService.OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderService.OrderItemRequest> items) { this.items = items; }
    }
}
