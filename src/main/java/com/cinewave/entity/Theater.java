package com.cinewave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Theater entity — a physical cinema location.
 * Soft-deleted to preserve booking history.
 */
@Entity
@Table(name = "theaters", indexes = {
    @Index(name = "idx_theater_city", columnList = "city")
})
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @Size(max = 100)
    @Column(length = 100)
    private String state = "Andhra Pradesh";

    @Size(max = 100)
    @Column(length = 100)
    private String district = "";

    @Size(max = 300)
    private String address;

    @Size(max = 500)
    private String facilities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Size(max = 100)
    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Size(max = 50)
    @Column(name = "discovery_source", length = 50)
    private String discoverySource = "MANUAL"; // "DIRECTORY_API", "CSV_IMPORT", "MANUAL"

    @Size(max = 50)
    @Column(name = "approval_status", length = 50)
    private String approvalStatus = "APPROVED"; // "DISCOVERED", "APPROVED", "PARTNER_ACTIVE", "REJECTED"

    @Column(name = "verified_partner")
    private Boolean verifiedPartner = Boolean.TRUE;

    @Size(max = 255)
    @Column(name = "normalized_key", length = 255)
    private String normalizedKey;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Size(max = 20)
    @Column(name = "pincode", length = 20)
    private String pincode;

    @Size(max = 255)
    @Column(name = "website", length = 255)
    private String website;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Screen> screens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Theater() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getDiscoverySource() { return discoverySource; }
    public void setDiscoverySource(String discoverySource) { this.discoverySource = discoverySource; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public boolean isVerifiedPartner() { return Boolean.TRUE.equals(verifiedPartner); }
    public void setVerifiedPartner(Boolean verifiedPartner) { this.verifiedPartner = Boolean.TRUE.equals(verifiedPartner); }

    public String getNormalizedKey() { return normalizedKey; }
    public void setNormalizedKey(String normalizedKey) { this.normalizedKey = normalizedKey; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Screen> getScreens() { return screens; }
    public void setScreens(List<Screen> screens) { this.screens = screens; }
}
