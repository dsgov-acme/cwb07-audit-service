package io.nuvalence.platform.audit.service.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mappings that can be reused for all <code>AuditEventEntity</code> subclasses.
 */
@Retention(RetentionPolicy.CLASS)
/*@Mapping(target = "eventData.schema", source = "request.eventData.schema")
@Mapping(target = "eventData.activityType", source = "request.eventData.activityType")
@Mapping(target = "eventData.data", source = "request.eventData.data")*/
@Mapping(target = "summary", source = "request.summary")
@Mapping(target = "eventData.type", source = "request.eventData.type")
@Mapping(target = "links.systemOfRecord", source = "request.links.systemOfRecord")
@Mapping(target = "links.relatedBusinessObjects", source = "request.links.relatedBusinessObjects")
@Mapping(target = "requestContext", source = "request.requestContext")
public @interface ToAuditEvent {}
