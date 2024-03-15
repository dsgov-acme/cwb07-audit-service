package io.nuvalence.platform.audit.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nuvalence.events.event.AuditEvent;
import io.nuvalence.events.event.dto.ActivityEventData;
import io.nuvalence.events.event.dto.AuditEventDataBase;
import io.nuvalence.events.event.dto.BusinessObjectMetadata;
import io.nuvalence.events.event.dto.StateChangeEventData;
import io.nuvalence.platform.audit.service.config.exception.ProvidedDataException;
import io.nuvalence.platform.audit.service.domain.ActivityEventEntity;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.domain.StateChangeEventEntity;
import io.nuvalence.platform.audit.service.domain.enums.TypeEnum;
import io.nuvalence.platform.audit.service.error.ApiException;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.utils.SamplesUtil;
import io.nuvalence.platform.audit.service.utils.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize"})
class AuditEventMapperTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID ENTITY_ID = UUID.fromString("1190241c-5eae-11ec-bf63-0242ac130002");
    private static final String ENTITY_TYPE = "orders";

    private AuditEventMapper auditEventMapper;

    @BeforeEach
    void setUp() {
        auditEventMapper = Mappers.getMapper(AuditEventMapper.class);
    }

    @Test
    void toEntity_Activity() throws Exception {
        AuditEventRequest request = TestUtil.Data.ACTIVITY_REQUEST.readJson();
        ActivityEventEntity entity =
                SamplesUtil.readJsonFile(TestUtil.ACTIVITY_ENTITY_JSON, ActivityEventEntity.class);
        AuditEvent event = SamplesUtil.readJsonFile(TestUtil.ACTIVITY_EVENT_JSON, AuditEvent.class);

        assertThat(auditEventMapper.toAuditEvent(request, ENTITY_ID, ENTITY_TYPE))
                .usingRecursiveComparison()
                .ignoringFields("metadata.id", "metadata.correlationId")
                .isEqualTo(event);

        assertThat(auditEventMapper.toEntity(event))
                .usingRecursiveComparison()
                .ignoringFields("metadata.id", "metadata.correlationId")
                .isEqualTo(entity);
    }

    @Test
    void toActivityEventEntity_givenStateChangeData_ShouldThrow() {
        StateChangeEventData stateChangeEventData = new StateChangeEventData();
        stateChangeEventData.setType("StateChangeEventData");
        var event = new AuditEvent();
        event.setEventData(stateChangeEventData);
        Assertions.assertThrows(
                ApiException.class, () -> auditEventMapper.toActivityEventEntity(event));
    }

    @Test
    void toEntity_StateChange() throws Exception {
        AuditEventRequest request = TestUtil.Data.STATE_CHANGE_REQUEST.readJson();
        AuditEvent event = TestUtil.Data.STATE_CHANGE_EVENT.readJson();
        StateChangeEventEntity entity =
                SamplesUtil.readJsonFile(
                        TestUtil.STATE_CHANGE_ENTITY_JSON, StateChangeEventEntity.class);

        assertThat(auditEventMapper.toAuditEvent(request, ENTITY_ID, ENTITY_TYPE))
                .usingRecursiveComparison()
                .ignoringFields("metadata.id", "metadata.correlationId")
                .isEqualTo(event);

        assertThat(auditEventMapper.toEntity(event))
                .usingRecursiveComparison()
                .ignoringFields("metadata.id", "metadata.correlationId")
                .isEqualTo(entity);
    }

    @Test
    void toStateChangeEventEntity_givenActivityData_ShouldThrow() {
        ActivityEventData activityEventData = new ActivityEventData();
        activityEventData.setType("ActivityEventData");
        var event = new AuditEvent();
        event.setEventData(activityEventData);
        Assertions.assertThrows(
                ApiException.class, () -> auditEventMapper.toStateChangeEventEntity(event));
    }

    @Test
    void toStateChangeEventEntityWithNoTypeShouldThrow() {
        var event = new AuditEvent();
        event.setEventData(new AuditEventDataBase());
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> auditEventMapper.toStateChangeEventEntity(event));
    }

    @Test
    void toActivityEventEntityWithOutTypeShouldThrow() {
        var event = new AuditEvent();
        event.setEventData(new AuditEventDataBase());
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> auditEventMapper.toActivityEventEntity(event));
    }

    @Test
    void fromEntity_Activity() throws Exception {
        AuditEvent event = TestUtil.Data.ACTIVITY_EVENT.readJson();
        event.setBusinessObject(createEntityModel());
        event.getMetadata().setId(EVENT_ID);

        ActivityEventEntity entity = TestUtil.Data.ACTIVITY_ENTITY.readJson();
        entity.setEventId(EVENT_ID);

        assertThat(auditEventMapper.fromEntity(entity))
                .usingRecursiveComparison()
                .ignoringFields("metadata.id", "metadata.correlationId")
                .isEqualTo(event);
    }

    private BusinessObjectMetadata createEntityModel() {
        var businessObjectMetadata = new BusinessObjectMetadata();
        businessObjectMetadata.setId(ENTITY_ID);
        businessObjectMetadata.setType(ENTITY_TYPE);
        return businessObjectMetadata;
    }

    @Test
    void fromEntity_StateChange() throws Exception {
        AuditEvent event = TestUtil.Data.STATE_CHANGE_EVENT.readJson();
        event.setBusinessObject(createEntityModel());
        event.getMetadata().setId(EVENT_ID);

        StateChangeEventEntity entity = TestUtil.Data.STATE_CHANGE_ENTITY.readJson();
        entity.setEventId(EVENT_ID);

        assertThat(auditEventMapper.fromEntity(entity))
                .usingRecursiveComparison()
                .ignoringFields("metadata.id", "metadata.correlationId")
                .isEqualTo(event);
    }

    @Test
    void fromEntities_GivenListOfEntities_ShouldConvertAll() throws IOException {
        List<AuditEventEntity> entities =
                List.of(
                        TestUtil.Data.STATE_CHANGE_ENTITY.readJson(),
                        TestUtil.Data.ACTIVITY_ENTITY.readJson());

        List<AuditEvent> expected =
                List.of(
                        TestUtil.Data.STATE_CHANGE_EVENT.readJson(),
                        TestUtil.Data.ACTIVITY_EVENT.readJson());

        expected.forEach(
                e ->
                        e.setBusinessObject(
                                BusinessObjectMetadata.builder()
                                        .type(ENTITY_TYPE)
                                        .id(ENTITY_ID)
                                        .build()));

        assertThat(auditEventMapper.fromEntities(entities))
                .usingRecursiveComparison()
                .ignoringFields("metadata.correlationId", "metadata.id")
                .isEqualTo(expected);
    }

    @Test
    void testGetEnumFromRequest() {
        // Create a sample AuditEventRequest with a known type
        AuditEvent event = new AuditEvent();
        AuditEventDataBase eventData = new AuditEventDataBase();
        eventData.setType("StateChangeEventData");
        event.setEventData(eventData);

        // Call the method to get the enum
        TypeEnum result = auditEventMapper.getEnumFromEvent(event);

        // Assert that the result is as expected
        assertEquals(TypeEnum.STATE_CHANGE_EVENT_DATA, result);
    }

    @Test
    void testGetEnumFromEntity() {
        // Create a sample AuditEventRequest with a known type
        AuditEventEntity entity = new AuditEventEntity();
        entity.setType(TypeEnum.STATE_CHANGE_EVENT_DATA);

        // Call the method to get the enum
        String result = auditEventMapper.getEnumFromEntity(entity);

        // Assert that the result is as expected
        assertEquals("StateChangeEventData", result);
    }

    @Test
    void testTypeEnumtoString() {
        // Create a sample AuditEventRequest with a known type
        TypeEnum typeEnum = TypeEnum.STATE_CHANGE_EVENT_DATA;

        // Call the method to get the enum
        String result = typeEnum.toString();

        // Assert that the result is as expected
        assertEquals("StateChangeEventData", result);
    }

    @Test
    void toAuditEvent() throws Exception {
        AuditEventRequest request = TestUtil.Data.ACTIVITY_REQUEST.readJson();
        AuditEvent event = SamplesUtil.readJsonFile(TestUtil.ACTIVITY_EVENT_JSON, AuditEvent.class);

        assertThat(auditEventMapper.toAuditEvent(request, ENTITY_ID, ENTITY_TYPE))
                .usingRecursiveComparison()
                .ignoringFields("metadata.id", "metadata.correlationId")
                .isEqualTo(event);
    }

    @Test
    void toAuditEvent_invalidType() throws Exception {
        AuditEventRequest request = TestUtil.Data.ACTIVITY_REQUEST.readJson();
        request.getEventData().setType("invalid");
        ProvidedDataException thrownException =
                Assertions.assertThrows(
                        ProvidedDataException.class,
                        () -> auditEventMapper.toAuditEvent(request, ENTITY_ID, ENTITY_TYPE));

        assertEquals("Invalid eventData type: invalid", thrownException.getMessage());
    }

    @Test
    void toAuditEventModel_invalidType() {
        AuditEvent request =
                AuditEvent.builder()
                        .eventData(AuditEventDataBase.builder().type("invalid").build())
                        .build();
        ProvidedDataException thrownException =
                Assertions.assertThrows(
                        ProvidedDataException.class,
                        () -> auditEventMapper.toAuditEventModel(request));

        assertEquals("Invalid eventData type: invalid", thrownException.getMessage());
    }

    @Test
    void toEntity_whenEventIsNull_ShouldReturnNull() {
        AuditEvent event = null;
        assertNull(auditEventMapper.toEntity(event));
    }

    @Test
    void toEntity_whenEventDataIsNull_ShouldReturnNull() {
        AuditEvent event = new AuditEvent();
        event.setEventData(null);
        assertNull(auditEventMapper.toEntity(event));
    }

    @Test
    void toEntity_whenEventTypeIsNull_AndEventDataIsActivity_ShouldReturnActivityEntity()
            throws IOException {
        AuditEvent event = SamplesUtil.readJsonFile(TestUtil.ACTIVITY_EVENT_JSON, AuditEvent.class);
        event.getEventData().setType(null);
        ActivityEventData activityEventData = new ActivityEventData();
        event.setEventData(activityEventData);
        assertNotNull(auditEventMapper.toEntity(event));
        assertTrue(auditEventMapper.toEntity(event) instanceof ActivityEventEntity);
    }

    @Test
    void
            toEntity_whenEventTypeIsNull_AndEventDataIsStateChange_ShouldReturnStateChangeEventEntity() {
        AuditEvent event = new AuditEvent();
        StateChangeEventData stateChangeEventData = new StateChangeEventData();
        event.setEventData(stateChangeEventData);
        assertNotNull(auditEventMapper.toEntity(event));
        assertTrue(auditEventMapper.toEntity(event) instanceof StateChangeEventEntity);
    }

    @Test
    void toEntity_whenEventTypeIsNull_AndEventDataIsNotRecognized_ShouldReturnNull() {
        AuditEvent event = new AuditEvent();
        event.setEventData(new AuditEventDataBase());
        assertNull(auditEventMapper.toEntity(event));
    }
}
