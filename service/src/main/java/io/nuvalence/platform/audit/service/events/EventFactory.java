package io.nuvalence.platform.audit.service.events;

import io.nuvalence.events.event.Event;
import io.nuvalence.events.event.EventMetadata;
import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.logging.util.CorrelationIdContext;
import io.nuvalence.platform.audit.service.events.models.ApplicationRoles;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Factory for creating dsgov events.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventFactory {

    private static final String APPLICATION_NAME = "audit-service";

    /**
     * Create a RoleReportingEvent.
     *
     * @param appRoles the application roles
     * @return the event
     */
    public static RoleReportingEvent createRoleReportingEvent(ApplicationRoles appRoles) {

        RoleReportingEvent event =
                RoleReportingEvent.builder()
                        .name(appRoles.getName())
                        .roles(appRoles.getRoles())
                        .build();

        event.setMetadata(generateEventMetadata(event.getClass()));

        return event;
    }

    private static EventMetadata generateEventMetadata(Class<? extends Event> eventClass) {
        return EventMetadata.builder()
                .id(UUID.randomUUID())
                .type(eventClass.getSimpleName())
                .originatorId(APPLICATION_NAME)
                .timestamp(OffsetDateTime.now())
                .correlationId(CorrelationIdContext.getCorrelationId())
                .build();
    }
}
