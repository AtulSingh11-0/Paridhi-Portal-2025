package com.megatronix.paridhi.dto.request;

import com.megatronix.paridhi.constant.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreationRequest {
    
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 64, message = "Password must be between 6 adn 64 characters")
    private String password;

    private static final Role role = Role.ROLE_ADMIN;

    public Role getRole() {
        return role;
    }
}
