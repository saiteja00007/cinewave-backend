package com.cinewave.controller;

import com.cinewave.dto.PaymentRequest;
import com.cinewave.dto.PaymentResponse;
import com.cinewave.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) String method,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        Long finalBookingId = bookingId;
        String finalMethod = method;
        if (finalBookingId == null && body != null && body.containsKey("bookingId")) {
            finalBookingId = ((Number) body.get("bookingId")).longValue();
        }
        if (finalMethod == null && body != null && body.containsKey("paymentMethod")) {
            finalMethod = (String) body.get("paymentMethod");
        }
        if (finalMethod == null && body != null && body.containsKey("method")) {
            finalMethod = (String) body.get("method");
        }
        if (finalBookingId == null) {
            throw new com.cinewave.exception.BadRequestException("bookingId is required to initiate payment");
        }
        return ResponseEntity.ok(paymentService.initiatePayment(finalBookingId, finalMethod != null ? finalMethod : "UPI"));
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getBookingPayments(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId));
    }
}
