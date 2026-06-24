package de.htw_berlin.kindercare.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParentMessageRequest(
        @NotBlank(message = "A message is required.")
        @Size(max = 4000, message = "Message must not exceed 4000 characters.")
        String message
) { }
