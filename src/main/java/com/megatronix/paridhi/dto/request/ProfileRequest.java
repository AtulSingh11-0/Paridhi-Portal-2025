package com.megatronix.paridhi.dto.request;

import com.megatronix.paridhi.constant.Department;
import com.megatronix.paridhi.constant.Year;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Contact number cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact must be 10 digits")
    private String contact;
    
    @NotBlank(message = "College name cannot be empty")
    private String college;
    
    @NotNull(message = "Year cannot be null")
    private Year year;
    
    @NotNull(message = "Department cannot be null")
    private Department department;
    
    @NotBlank(message = "Roll number cannot be empty")
    private String rollNo;
}
