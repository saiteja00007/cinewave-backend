package com.cinewave.dto;

import com.cinewave.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private String paymentReference;
    private String transactionId;
    private Long bookingId;
    private String bookingReference;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String gateway;
    private LocalDateTime createdAt;

    public PaymentResponse() {}

    public static PaymentResponse fromEntity(Payment p) {
        PaymentResponse dto = new PaymentResponse();
        dto.setId(p.getId());
        dto.setPaymentReference(p.getPaymentReference());
        dto.setTransactionId(p.getTransactionId());
        if (p.getBooking() != null) {
            dto.setBookingId(p.getBooking().getId());
            dto.setBookingReference(p.getBooking().getBookingReference());
        }
        dto.setAmount(p.getAmount());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setStatus(p.getStatus());
        dto.setGateway(p.getGateway());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
