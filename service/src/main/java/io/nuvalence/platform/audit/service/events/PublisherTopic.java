package io.nuvalence.platform.audit.service.events;

import lombok.Getter;

/**
 * Enumerates the topics that can be published to.
 */
@Getter
public enum PublisherTopic {
    AUDIT_EVENTS_RECORDING,
    APPLICATION_ROLE_REPORTING
}
