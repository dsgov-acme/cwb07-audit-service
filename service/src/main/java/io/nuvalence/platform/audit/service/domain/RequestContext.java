package io.nuvalence.platform.audit.service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Request context for audit event.
 */
@Embeddable
@Getter
@Setter
public class RequestContext {
    @Column
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID userId;

    @Column
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Column
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID originatorId;

    @Column
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID requestId;

    @Column
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID traceId;

    @Column
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID spanId;
}
