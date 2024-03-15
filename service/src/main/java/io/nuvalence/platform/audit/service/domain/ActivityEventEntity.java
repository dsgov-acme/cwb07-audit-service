package io.nuvalence.platform.audit.service.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * An audit event which indicates some activity ocurred on some entity.
 */
@Getter
@Setter
@Entity
public class ActivityEventEntity extends AuditEventEntity {}
