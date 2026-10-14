package com.cinewave.dto;

import com.cinewave.entity.Role;
import com.cinewave.entity.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OwnerResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private List<TheaterResponse> assignedTheaters = new ArrayList<>();

    public OwnerResponse() {}

    public static OwnerResponse fromEntity(User user, List<TheaterResponse> theaters) {
        OwnerResponse dto = new OwnerResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        if (theaters != null) {
            dto.setAssignedTheaters(theaters);
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<TheaterResponse> getAssignedTheaters() { return assignedTheaters; }
    public void setAssignedTheaters(List<TheaterResponse> assignedTheaters) { this.assignedTheaters = assignedTheaters; }
}
