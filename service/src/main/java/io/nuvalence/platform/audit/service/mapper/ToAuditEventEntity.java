package io.nuvalence.platform.audit.service.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mappings that can be reused for all <code>AuditEventEntity</code> subclasses.
 */
@Retention(RetentionPolicy.CLASS)
@Mapping(target = "businessObjectId", source = "event.businessObject.id")
@Mapping(target = "businessObjectType", source = "event.businessObject.type")
@Mapping(target = "schema", source = "event.eventData.schema")
@Mapping(target = "timestamp", source = "event.metadata.timestamp")
@Mapping(target = "summary", source = "event.summary")
@Mapping(target = "type", expression = "java(getEnumFromEvent(event))")
@Mapping(target = "systemOfRecord", source = "event.links.systemOfRecord")
@Mapping(target = "relatedBusinessObjects", source = "event.links.relatedBusinessObjects")
@Mapping(target = "requestContext", source = "event.requestContext")
public @interface ToAuditEventEntity {}
