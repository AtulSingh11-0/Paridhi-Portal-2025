package com.megatronix.paridhi.dto.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequest {
    
    @NotBlank(message = "Team name cannot be empty")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    private String teamName;
    
    @NotNull(message = "Event ID cannot be null")
    private Long eventId;
    
    @NotBlank(message = "Team leader email cannot be empty")
    @Email(message = "Team leader email should be valid")
    private String teamLeaderEmail;
    
    @NotEmpty(message = "GID list cannot be empty")
    private List<String> gidList;
    
    @NotBlank(message = "Contact number cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact must be 10 digits")
    private String contact;
}
