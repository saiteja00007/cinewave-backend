package com.cinewave.dto;

import com.cinewave.entity.Theater;

public class TheaterResponse {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String facilities;
    private boolean active;
    private int screenCount;

    private String state;
    private String district;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    // Discovery & Directory Attributes
    private String providerId;
    private String discoverySource;
    private String approvalStatus;
    private boolean verifiedPartner;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String pincode;
    private String website;
    private boolean bookable;

    public static TheaterResponse fromEntity(Theater t) {
        TheaterResponse dto = new TheaterResponse();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setCity(t.getCity());
        dto.setState(t.getState());
        dto.setDistrict(t.getDistrict());
        dto.setAddress(t.getAddress());
        dto.setFacilities(t.getFacilities());
        dto.setActive(t.isActive());
        dto.setScreenCount(t.getScreens() != null ? t.getScreens().size() : 0);
        if (t.getOwner() != null) {
            dto.setOwnerId(t.getOwner().getId());
            dto.setOwnerName(t.getOwner().getName());
            dto.setOwnerEmail(t.getOwner().getEmail());
        }

        dto.setProviderId(t.getProviderId());
        dto.setDiscoverySource(t.getDiscoverySource() != null ? t.getDiscoverySource() : "MANUAL");
        dto.setApprovalStatus(t.getApprovalStatus() != null ? t.getApprovalStatus() : "APPROVED");
        dto.setVerifiedPartner(t.isVerifiedPartner());
        dto.setLatitude(t.getLatitude());
        dto.setLongitude(t.getLongitude());
        dto.setPhone(t.getPhone());
        dto.setPincode(t.getPincode());
        dto.setWebsite(t.getWebsite());
        // Only bookable when it's a verified CineWave partner AND has at least 1 screen configured
        dto.setBookable(t.isVerifiedPartner() && dto.getScreenCount() > 0);

        return dto;
    }

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
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getScreenCount() { return screenCount; }
    public void setScreenCount(int screenCount) { this.screenCount = screenCount; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getDiscoverySource() { return discoverySource; }
    public void setDiscoverySource(String discoverySource) { this.discoverySource = discoverySource; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public boolean isVerifiedPartner() { return verifiedPartner; }
    public void setVerifiedPartner(boolean verifiedPartner) { this.verifiedPartner = verifiedPartner; }
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
    public boolean isBookable() { return bookable; }
    public void setBookable(boolean bookable) { this.bookable = bookable; }
}
