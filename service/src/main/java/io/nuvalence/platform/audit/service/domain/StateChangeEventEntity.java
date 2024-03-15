package io.nuvalence.platform.audit.service.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * An audit event which indicates an entity's state has changed.
 */
@Getter
@Setter
@Entity
public class StateChangeEventEntity extends AuditEventEntity {
    private String newState;
    private String oldState;
}
