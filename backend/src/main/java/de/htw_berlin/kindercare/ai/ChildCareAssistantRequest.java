package de.htw_berlin.kindercare.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChildCareAssistantRequest(
        @NotBlank(message = "A request type is required.")
        @Pattern(regexp = "MEDICATION|SYMPTOMS|ALLERGY|INCIDENT", message = "Type must be MEDICATION, SYMPTOMS, ALLERGY, or INCIDENT.")
        String type,
        @NotBlank(message = "A message is required.")
        @Size(max = 4000, message = "Message must not exceed 4000 characters.")
        String message
) { }
