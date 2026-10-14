package com.cinewave.dto;

import com.cinewave.entity.Role;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 20)
    private String phone;

    private Role role;

    private Boolean active;

    public UpdateUserRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
