package com.megatronix.paridhi.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    
    @NotNull(message = "Domain cannot be null")
    private Domain domain;
    
    @NotBlank(message = "Event name cannot be empty")
    @Size(min = 2, max = 100, message = "Event name must be between 2 and 100 characters")
    private String name;
    
    @NotNull(message = "Event type cannot be null")
    private EventType eventType;
    
    @NotNull(message = "Event date cannot be null")
    private LocalDateTime eventDate;
    
    @NotBlank(message = "Description cannot be empty")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;
    
    @NotBlank(message = "Venue cannot be empty")
    private String venue;
    
    @NotNull(message = "Coordinator details cannot be empty")
    private List<String> coordinatorDetails;
    
    private String eventPictureUrl;
    
    private String ruleBook;
    
    @NotNull(message = "Min players cannot be null")
    @Min(value = 1, message = "Min players must be at least 1")
    private Integer minPlayers;
    
    @NotNull(message = "Max players cannot be null")
    @Min(value = 1, message = "Max players must be at least 1")
    private Integer maxPlayers;
    
    @NotNull(message = "Registration fee cannot be null")
    @Min(value = 0, message = "Registration fee must be non-negative")
    private Double registrationFee;
    
    private boolean isRegistrationOpen;
    
    @NotNull(message = "Prize pool cannot be null")
    @Min(value = 0, message = "Prize pool must be non-negative")
    private Double prizePool;
}
