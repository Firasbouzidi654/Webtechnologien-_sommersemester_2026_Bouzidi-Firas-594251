package de.htw_berlin.kindercare.ai;

import de.htw_berlin.kindercare.config.RoleAccess;
import jakarta.validation.Valid;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class ChildCareAssistantController {
    private final ChildCareAssistantService service;

    public ChildCareAssistantController(ChildCareAssistantService service) {
        this.service = service;
    }

    @PostMapping("/childcare-assistant")
    public ChildCareAssistantResponse ask(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @Valid @RequestBody ChildCareAssistantRequest request
    ) {
        RoleAccess.require(role, "STAFF", "ADMIN");
        return new ChildCareAssistantResponse(service.ask(request));
    }
}
