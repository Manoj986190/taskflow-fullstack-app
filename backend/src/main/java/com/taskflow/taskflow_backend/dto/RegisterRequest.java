package com.taskflow.taskflow_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "email is required")
    private String email;
    
    @Size(min = 8,message = "password must be at least 8 characters")
    private String password;

    private String ConfirmPassword;
    
}
