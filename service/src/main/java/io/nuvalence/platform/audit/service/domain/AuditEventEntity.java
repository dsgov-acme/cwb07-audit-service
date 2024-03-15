package io.nuvalence.platform.audit.service.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.nuvalence.auth.access.AccessResource;
import io.nuvalence.platform.audit.service.domain.enums.TypeEnum;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Audit event entity.
 */
@Getter
@Setter
@Entity
@Table(name = "audit_events")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@AccessResource("audit-event")
public class AuditEventEntity {
    @Id
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID eventId;

    @Column private String schema;

    @Column(length = 32, nullable = false)
    @Convert(converter = TypeEnumConverter.class)
    private TypeEnum type;

    @Column
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID businessObjectId;

    @Column(length = 64)
    private String businessObjectType;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    private String summary;

    @Column private String systemOfRecord;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(
            name = "audit_events_related_business_objects",
            joinColumns = @JoinColumn(name = "audit_event_id"))
    @Column(name = "related_business_object", nullable = false)
    private Set<String> relatedBusinessObjects;

    @Embedded private RequestContext requestContext;

    private String activityType;

    private String data;
}
