package io.nuvalence.platform.audit.service.controllers;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.auth.token.profiles.ProfileAccessLevel;
import io.nuvalence.auth.token.profiles.ProfileLink;
import io.nuvalence.auth.token.profiles.ProfileType;
import io.nuvalence.auth.util.SecurityContextUtility;
import io.nuvalence.platform.audit.service.config.exception.ProvidedDataException;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.generated.controllers.AuditEventsApiDelegate;
import io.nuvalence.platform.audit.service.generated.models.AuditEventId;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.generated.models.AuditEventsPage;
import io.nuvalence.platform.audit.service.mapper.AuditEventMapper;
import io.nuvalence.platform.audit.service.mapper.PagingMetadataMapper;
import io.nuvalence.platform.audit.service.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller layer for audit service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditEventApiDelegateImpl implements AuditEventsApiDelegate {
    private final AuditEventService auditEventService;

    private final AuthorizationHandler authorizationHandler;

    private final PagingMetadataMapper pagingMetadataMapper;
    private final AuditEventMapper auditEventMapper;

    @Override
    public ResponseEntity<AuditEventsPage> getEvents(
            String businessObjectType,
            UUID businessObjectId,
            String sortOrder,
            String sortBy,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            Integer pageNumber,
            Integer pageSize) {

        if (!authorizationHandler.isAllowed("view", AuditEventEntity.class)
                && !validateProfileAccessRequest(
                        businessObjectId, businessObjectType, ProfileAccessLevel.ADMIN)) {

            throw new AccessDeniedException("Forbidden request.");
        }

        var events =
                auditEventService.findAuditEvents(
                        businessObjectType,
                        businessObjectId,
                        startTime,
                        endTime,
                        pageNumber,
                        pageSize,
                        sortOrder,
                        sortBy);

        return ResponseEntity.ok(this.createAuditEventsPage(events));
    }

    @Override
    public ResponseEntity<AuditEventId> postEvent(
            String businessObjectType, UUID businessObjectId, AuditEventRequest body) {
        if (!authorizationHandler.isAllowed("create", AuditEventEntity.class)) {
            throw new AccessDeniedException("You do not have permission to create this resource.");
        }

        log.debug(
                "Received audit event request for business object type {} and id {}",
                businessObjectType,
                businessObjectId);

        var auditEvent = auditEventMapper.toAuditEvent(body, businessObjectId, businessObjectType);
        var eventId = new AuditEventId().eventId(auditEvent.getMetadata().getId());
        auditEventService.publishAuditEvent(auditEvent);

        return ResponseEntity.status(201).body(eventId);
    }

    private AuditEventsPage createAuditEventsPage(Page<AuditEventEntity> page) {
        return new AuditEventsPage()
                .events(
                        auditEventMapper.fromEntities(page.getContent()).stream()
                                .map(auditEventMapper::toAuditEventModel)
                                .toList())
                .pagingMetadata(pagingMetadataMapper.toPagingMetadata(page));
    }

    /**
     * Validates if the request is related to a profile, and if the user has at least the access level wanted.
     * 
     * @param profileId The profile id to validate access.
     * @param profileTypeExpected The expected profile type.
     * @param accessLevelWanted The wanted access level.
     * 
     * @return True if: profile type is valid and user has access to the profile and to the access level.
     *         False otherwise, including if profileTypeExpected is invalid.
     */
    private boolean validateProfileAccessRequest(
            UUID profileId, String profileTypeExpected, ProfileAccessLevel accessLevelWanted) {

        ProfileType profileTypeExpectedEnum = null;
        try {
            profileTypeExpectedEnum = ProfileType.fromValue(profileTypeExpected);
        } catch (IllegalArgumentException e) {
            log.trace("Invalid expected profile type: {}", profileTypeExpected);
            return false;
        }

        Optional<ProfileLink> optionalLink =
                SecurityContextUtility.getAuthenticatedUserProfileLinks().stream()
                        .filter(link -> link.getProfileId().equals(profileId))
                        .findFirst();

        if (optionalLink.isEmpty()) {
            return false;
        }

        ProfileLink profileLink = optionalLink.get();
        ProfileAccessLevel linkAccessLevel =
                ProfileAccessLevel.fromValue(profileLink.getAccessLevel());
        ProfileType linkProfileType = ProfileType.fromValue(profileLink.getProfileType());

        if (profileTypeExpectedEnum != linkProfileType) {
            throw new ProvidedDataException(
                    "The specified profile is not of the expected type: "
                            + profileTypeExpectedEnum.getLabel());
        }

        return linkAccessLevel.hasEqualsOrMoreAccess(accessLevelWanted);
    }
}
