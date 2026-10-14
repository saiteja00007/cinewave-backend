package com.cinewave.service;

import com.cinewave.dto.PaymentRequest;
import com.cinewave.dto.PaymentResponse;
import com.cinewave.entity.Booking;
import com.cinewave.entity.BookingStatus;
import com.cinewave.entity.Payment;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.BookingRepository;
import com.cinewave.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${cinewave.razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${cinewave.razorpay.key-secret:}")
    private String razorpayKeySecret;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    public boolean isRazorpayConfigured() {
        return StringUtils.hasText(razorpayKeyId) && StringUtils.hasText(razorpayKeySecret);
    }

    public PaymentResponse initiatePayment(Long bookingId, String paymentMethod) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Check if there is already a payment record
        List<Payment> existingPayments = paymentRepository.findByBookingId(bookingId);
        for (Payment p : existingPayments) {
            if ("SUCCESSFUL".equalsIgnoreCase(p.getStatus())) {
                return PaymentResponse.fromEntity(p);
            }
        }

        String paymentRef = "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Payment payment = new Payment();
        payment.setPaymentReference(paymentRef);
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(StringUtils.hasText(paymentMethod) ? paymentMethod : "UPI");
        payment.setStatus("PENDING");
        payment.setGateway(isRazorpayConfigured() ? "RAZORPAY" : "SANDBOX");

        payment = paymentRepository.save(payment);
        return PaymentResponse.fromEntity(payment);
    }

    public PaymentResponse verifyPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));

        String method = StringUtils.hasText(request.getPaymentMethod()) ? request.getPaymentMethod() : "UPI";
        String gateway = isRazorpayConfigured() ? "RAZORPAY" : "SANDBOX";

        // Verification logic
        if (isRazorpayConfigured()) {
            if (!StringUtils.hasText(request.getRazorpayPaymentId()) ||
                !StringUtils.hasText(request.getRazorpayOrderId()) ||
                !StringUtils.hasText(request.getRazorpaySignature())) {
                throw new BadRequestException("Razorpay payment credentials are required for verification");
            }

            boolean signatureValid = verifyRazorpaySignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature(),
                    razorpayKeySecret
            );

            if (!signatureValid) {
                log.error("Payment signature mismatch for booking ID: {}", booking.getId());
                throw new BadRequestException("Invalid payment signature. Transaction rejected.");
            }
        }

        // Generate payment reference and transaction ID if not present
        String paymentRef = "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String txId = StringUtils.hasText(request.getTransactionId())
                ? request.getTransactionId()
                : (StringUtils.hasText(request.getRazorpayPaymentId()) ? request.getRazorpayPaymentId() : "TXN-" + System.currentTimeMillis());

        Payment payment = new Payment();
        payment.setPaymentReference(paymentRef);
        payment.setTransactionId(txId);
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setStatus("SUCCESSFUL");
        payment.setGateway(gateway);
        payment = paymentRepository.save(payment);

        // Update booking state atomically
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentMethod(method);
        booking.setPaymentStatus("SUCCESSFUL");
        booking.setPaymentReference(paymentRef);
        bookingRepository.save(booking);

        log.info("Payment verified successfully for booking: {} with ref: {} via {}",
                booking.getBookingReference(), paymentRef, gateway);

        return PaymentResponse.fromEntity(payment);
    }

    private boolean verifyRazorpaySignature(String orderId, String paymentId, String signature, String secret) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return MessageDigest.isEqual(hexString.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error verifying Razorpay HMAC signature", e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
