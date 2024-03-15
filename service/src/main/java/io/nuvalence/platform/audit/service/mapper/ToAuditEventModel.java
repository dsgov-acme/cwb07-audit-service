package io.nuvalence.platform.audit.service.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mappings that can be reused for all <code>AuditEventEntity</code> subclasses.
 */
@Retention(RetentionPolicy.CLASS)
@Mapping(target = "eventData.schema", source = "event.eventData.schema")
@Mapping(target = "eventData.activityType", source = "event.eventData.activityType")
@Mapping(target = "eventData.data", source = "event.eventData.data")
@Mapping(target = "summary", source = "event.summary")
@Mapping(target = "timestamp", source = "event.metadata.timestamp")
@Mapping(target = "eventData.type", source = "event.eventData.type")
@Mapping(target = "links.systemOfRecord", source = "event.links.systemOfRecord")
@Mapping(target = "links.relatedBusinessObjects", source = "event.links.relatedBusinessObjects")
@Mapping(target = "requestContext", source = "event.requestContext")
public @interface ToAuditEventModel {}
