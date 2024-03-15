package io.nuvalence.platform.audit.service.events.models;

import io.nuvalence.events.event.dto.ApplicationRole;
import lombok.Getter;

import java.util.List;

/**
 * Represents a list of roles that this application uses.
 */
@Getter
public class ApplicationRoles {
    private String name;
    private List<ApplicationRole> roles;
}
