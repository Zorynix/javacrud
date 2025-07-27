package crudjava.crudjava.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderEventDto {
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime eventTime;

    public OrderEventDto() {}

    public OrderEventDto(Long orderId, String orderNumber, Long customerId, String customerEmail,
                        String status, BigDecimal totalAmount, LocalDateTime eventTime) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.status = status;
        this.totalAmount = totalAmount;
        this.eventTime = eventTime;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
