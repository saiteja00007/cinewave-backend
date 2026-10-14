package com.cinewave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TheaterRequest {

    @NotBlank(message = "Theater name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state = "Telangana";

    @Size(max = 100)
    private String district;

    private Long ownerId;

    @Size(max = 300)
    private String address;

    @Size(max = 500)
    private String facilities;

    @Size(max = 100)
    private String providerId;

    @Size(max = 50)
    private String discoverySource = "MANUAL";

    @Size(max = 50)
    private String approvalStatus = "APPROVED";

    private Boolean verifiedPartner = true;

    private Double latitude;
    private Double longitude;

    @Size(max = 30)
    private String phone;

    @Size(max = 20)
    private String pincode;

    @Size(max = 255)
    private String website;

    public TheaterRequest() {}

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
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getDiscoverySource() { return discoverySource; }
    public void setDiscoverySource(String discoverySource) { this.discoverySource = discoverySource; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public Boolean getVerifiedPartner() { return verifiedPartner; }
    public void setVerifiedPartner(Boolean verifiedPartner) { this.verifiedPartner = verifiedPartner; }
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
}
