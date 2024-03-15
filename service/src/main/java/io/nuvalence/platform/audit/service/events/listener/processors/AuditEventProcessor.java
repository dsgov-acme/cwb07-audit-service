package io.nuvalence.platform.audit.service.events.listener.processors;

import io.nuvalence.events.event.AuditEvent;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.events.subscriber.EventProcessor;
import io.nuvalence.platform.audit.service.mapper.AuditEventMapper;
import io.nuvalence.platform.audit.service.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for processing audit events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventProcessor implements EventProcessor<AuditEvent> {

    private final AuditEventMapper auditEventMapper;
    private final AuditEventService auditEventService;

    @Override
    public Class<AuditEvent> getEventClass() {
        return AuditEvent.class;
    }

    @Override
    public void execute(AuditEvent event) throws EventProcessingException {

        try {
            log.debug(
                    "Received audit event request for business object type {} and id {}",
                    event.getBusinessObject().getType(),
                    event.getBusinessObject().getId());

            var eventEntity = auditEventMapper.toEntity(event);
            auditEventService.saveAuditEvent(eventEntity);
        } catch (Exception e) {
            throw new EventProcessingException(e);
        }
    }
}
