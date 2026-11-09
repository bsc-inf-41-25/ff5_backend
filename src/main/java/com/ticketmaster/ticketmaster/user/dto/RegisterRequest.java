package com.ticketmaster.ticketmaster.user.dto;

import com.ticketmaster.ticketmaster.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Removed @NotNull. Defaults to CUSTOMER if not provided by the frontend.
    private User.Role role = User.Role.CUSTOMER;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, User.Role role) {
        this.email = email;
        this.password = password;
        if (role != null) {
            this.role = role;
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { 
        this.role = (role != null) ? role : User.Role.CUSTOMER; 
    }
}