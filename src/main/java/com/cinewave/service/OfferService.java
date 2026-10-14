package com.cinewave.service;

import com.cinewave.dto.OfferValidationRequest;
import com.cinewave.dto.OfferValidationResponse;
import com.cinewave.entity.Offer;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@SuppressWarnings("null")
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public List<Offer> getAllActive() {
        return offerRepository.findByActiveTrue();
    }

    public List<Offer> getAll() {
        return offerRepository.findAll();
    }

    public Offer getById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
    }

    @Transactional
    public Offer create(Offer offer) {
        if (offerRepository.findByCodeIgnoreCaseAndActiveTrue(offer.getCode()).isPresent()) {
            throw new BadRequestException("An active offer with code '" + offer.getCode() + "' already exists");
        }
        return offerRepository.save(offer);
    }

    @Transactional
    public Offer update(Long id, Offer updated) {
        Offer existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDiscountType(updated.getDiscountType());
        existing.setDiscountValue(updated.getDiscountValue());
        existing.setMaxDiscount(updated.getMaxDiscount());
        existing.setMinOrderAmount(updated.getMinOrderAmount());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setUsageLimit(updated.getUsageLimit());
        existing.setActive(updated.isActive());
        return offerRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Offer existing = getById(id);
        existing.setActive(false);
        offerRepository.save(existing);
    }

    @Transactional
    public Offer toggleStatus(Long id) {
        Offer existing = getById(id);
        existing.setActive(!existing.isActive());
        return offerRepository.save(existing);
    }

    @Transactional
    public OfferValidationResponse validatePromo(OfferValidationRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return OfferValidationResponse.failure("Promo code cannot be empty");
        }

        String code = request.getCode().trim().toUpperCase();
        Offer offer = offerRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .orElse(null);

        if (offer == null) {
            return OfferValidationResponse.failure("Invalid promo code '" + code + "'");
        }

        LocalDate today = LocalDate.now();
        if (offer.getStartDate() != null && today.isBefore(offer.getStartDate())) {
            return OfferValidationResponse.failure("Offer '" + code + "' has not started yet");
        }

        if (offer.getEndDate() != null && today.isAfter(offer.getEndDate())) {
            return OfferValidationResponse.failure("Offer '" + code + "' has expired");
        }

        if (offer.getUsageLimit() != null && offer.getUsedCount() >= offer.getUsageLimit()) {
            return OfferValidationResponse.failure("Offer '" + code + "' usage limit reached");
        }

        double orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : 0.0;
        if (offer.getMinOrderAmount() != null && orderAmount < offer.getMinOrderAmount()) {
            return OfferValidationResponse.failure(
                    String.format("Minimum order amount of ₹%.0f required to use '%s'", offer.getMinOrderAmount(), code));
        }

        double discount = 0.0;
        if ("PERCENTAGE".equalsIgnoreCase(offer.getDiscountType())) {
            discount = orderAmount * (offer.getDiscountValue() / 100.0);
            if (offer.getMaxDiscount() != null && offer.getMaxDiscount() > 0) {
                discount = Math.min(discount, offer.getMaxDiscount());
            }
        } else { // FLAT
            discount = Math.min(offer.getDiscountValue(), orderAmount);
        }

        // Round to 2 decimals
        discount = Math.round(discount * 100.0) / 100.0;
        double finalAmount = Math.max(0.0, Math.round((orderAmount - discount) * 100.0) / 100.0);

        return new OfferValidationResponse(
                true,
                offer.getCode(),
                offer.getTitle(),
                discount,
                finalAmount,
                String.format("Promo code '%s' applied! Saved ₹%.0f", offer.getCode(), discount)
        );
    }

    @Transactional
    public void recordUsage(String code) {
        offerRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .ifPresent(offer -> {
                    offer.setUsedCount(offer.getUsedCount() + 1);
                    offerRepository.save(offer);
                });
    }
}
