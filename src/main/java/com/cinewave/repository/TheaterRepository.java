package com.cinewave.repository;

import com.cinewave.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    List<Theater> findByActiveTrue();

    Optional<Theater> findByProviderId(String providerId);

    Optional<Theater> findByNormalizedKey(String normalizedKey);

    boolean existsByProviderId(String providerId);

    boolean existsByNormalizedKey(String normalizedKey);

    List<Theater> findByCityAndActiveTrue(String city);

    List<Theater> findByCityIgnoreCaseAndActiveTrue(String city);

    List<Theater> findByStateAndActiveTrue(String state);

    List<Theater> findByStateIgnoreCaseAndActiveTrue(String state);

    List<Theater> findByStateIgnoreCaseAndCityIgnoreCaseAndActiveTrue(String state, String city);

    List<Theater> findByVerifiedPartnerTrueAndActiveTrue();

    List<Theater> findByCityIgnoreCaseAndVerifiedPartnerTrueAndActiveTrue(String city);

    List<Theater> findByApprovalStatusAndActiveTrue(String approvalStatus);

    List<Theater> findByDiscoverySourceAndActiveTrue(String discoverySource);

    List<Theater> findByOwnerIdAndActiveTrue(Long ownerId);

    List<Theater> findByOwnerId(Long ownerId);

    @Query("SELECT DISTINCT t.city FROM Theater t WHERE t.active = true ORDER BY t.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT t.state FROM Theater t WHERE t.state IS NOT NULL AND t.active = true ORDER BY t.state")
    List<String> findDistinctStates();

    @Query("SELECT DISTINCT t.district FROM Theater t WHERE t.state = :state AND t.district IS NOT NULL AND t.district <> '' AND t.active = true ORDER BY t.district")
    List<String> findDistinctDistrictsByState(@Param("state") String state);

    @Query("SELECT DISTINCT t.city FROM Theater t WHERE t.state = :state AND t.active = true ORDER BY t.city")
    List<String> findDistinctCitiesByState(@Param("state") String state);

    @Query("SELECT t FROM Theater t WHERE t.active = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.address) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Theater> searchTheaters(@Param("query") String query);

    long countByActiveTrue();

    long countByVerifiedPartnerTrueAndActiveTrue();
}
