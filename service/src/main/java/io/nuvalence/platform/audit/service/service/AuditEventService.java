package io.nuvalence.platform.audit.service.service;

import io.nuvalence.events.brokerclient.config.PublisherProperties;
import io.nuvalence.events.event.AuditEvent;
import io.nuvalence.events.event.service.EventGateway;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.error.ApiException;
import io.nuvalence.platform.audit.service.events.PublisherTopic;
import io.nuvalence.platform.audit.service.repository.AuditEventRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer to manage audit events.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class AuditEventService {
    private final AuditEventRepository auditEventRepository;
    private final PublisherProperties publisherProperties;
    private final EventGateway eventGateway;

    private static void checkTimeRange(OffsetDateTime startTime, OffsetDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw ApiException.Builder.badRequest(
                    "The startTime cannot be greater than the endTime.");
        }
    }

    static PageRequest createPageable(
            Integer pageNumber, Integer pageSize, String sortOrder, String sortBy) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
            return PageRequest.of(pageNumber, pageSize, sortDirection, sortBy);
        } catch (IllegalArgumentException e) {
            throw ApiException.Builder.badRequest(e.getMessage());
        }
    }

    /**
     * Queries audit events from db.
     *
     * @param businessObjectType Type of business object.
     * @param businessObjectId   Unique identifier for a business object of the specified type.
     * @param startTime          Specifies a start time (inclusive) for filtering results to events which occurred at
     *                           or after the specified time.
     * @param endTime            Specifies an end time (exclusive)for filtering results to events which occurred before
     *                           the specified time.
     * @param pageNumber         Results page number.
     * @param pageSize           Results page size.
     * @param sortOrder          Controls whether results are returned in chronologically ascending or descending order.
     * @param sortBy             Specifies the field to sort results by.
     * @return page object containing db query results and pagination metadata
     */
    @Transactional(readOnly = true)
    public Page<AuditEventEntity> findAuditEvents(
            String businessObjectType,
            UUID businessObjectId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            Integer pageNumber,
            Integer pageSize,
            String sortOrder,
            String sortBy) {
        checkTimeRange(startTime, endTime);

        return auditEventRepository.findAll(
                businessObjectType,
                businessObjectId,
                startTime,
                endTime,
                createPageable(pageNumber, pageSize, sortOrder, sortBy));
    }

    /**
     * Publish an Audit event to it's topic to be processed asynchronously.
     *
     * @param auditEvent audit event data
     * @throws NotFoundException if the topic is not found
     */
    public void publishAuditEvent(AuditEvent auditEvent) {

        Optional<String> fullyQualifiedTopicNameOptional =
                publisherProperties.getFullyQualifiedTopicName(
                        PublisherTopic.AUDIT_EVENTS_RECORDING.name());

        if (fullyQualifiedTopicNameOptional.isEmpty()) {
            throw new NotFoundException(
                    "Notification requests topic not found, topic name: "
                            + PublisherTopic.AUDIT_EVENTS_RECORDING.name());
        }

        eventGateway.publishEvent(auditEvent, fullyQualifiedTopicNameOptional.get());
    }

    /**
     * Persists an audit event to the database.
     *
     * @param entity audit event data
     */
    public void saveAuditEvent(AuditEventEntity entity) {
        try {
            auditEventRepository.save(entity);
            log.info(String.format("Audit event persisted - %s", entity.getEventId()));
        } catch (DuplicateKeyException ex) {
            log.info("Error persisting Audit event: ", ex);
        }
    }
}
