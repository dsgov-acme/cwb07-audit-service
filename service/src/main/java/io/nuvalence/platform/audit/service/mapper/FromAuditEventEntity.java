package io.nuvalence.platform.audit.service.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mappings that can be reused for all <code>AuditEventEntity</code> subclasses.
 */
@Retention(RetentionPolicy.CLASS)
// @Mapping(target = "metadata.id", source = "entity.eventId")
@Mapping(target = "businessObject.id", source = "entity.businessObjectId")
@Mapping(target = "businessObject.type", source = "entity.businessObjectType")
@Mapping(target = "eventData.schema", source = "entity.schema")
// @Mapping(target = "metadata.timestamp", source = "entity.timestamp")
@Mapping(target = "summary", source = "entity.summary")
@Mapping(target = "links.systemOfRecord", source = "entity.systemOfRecord")
@Mapping(target = "links.relatedBusinessObjects", source = "entity.relatedBusinessObjects")
@Mapping(target = "requestContext", source = "entity.requestContext")
public @interface FromAuditEventEntity {}
