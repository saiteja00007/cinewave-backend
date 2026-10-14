package com.cinewave.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class OwnerRequest {

    @NotBlank(message = "Owner name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 20)
    private String phone;

    private List<Long> assignedTheaterIds;

    public OwnerRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<Long> getAssignedTheaterIds() { return assignedTheaterIds; }
    public void setAssignedTheaterIds(List<Long> assignedTheaterIds) { this.assignedTheaterIds = assignedTheaterIds; }
}
