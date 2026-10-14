package com.cinewave.repository;

import com.cinewave.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByDestinationOrderByCreatedAtDesc(String destination);
    void deleteByDestination(String destination);
}
