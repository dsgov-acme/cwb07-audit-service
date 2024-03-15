package io.nuvalence.platform.audit.service.controllers;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.auth.token.profiles.ProfileAccessLevel;
import io.nuvalence.auth.token.profiles.ProfileLink;
import io.nuvalence.auth.token.profiles.ProfileType;
import io.nuvalence.auth.util.SecurityContextUtility;
import io.nuvalence.events.brokerclient.config.PublisherProperties;
import io.nuvalence.events.event.AuditEvent;
import io.nuvalence.events.event.service.EventGateway;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.error.ApiException;
import io.nuvalence.platform.audit.service.events.PublisherTopic;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.mapper.AuditEventMapper;
import io.nuvalence.platform.audit.service.repository.AuditEventRepository;
import io.nuvalence.platform.audit.service.service.AuditEventService;
import io.nuvalence.platform.audit.service.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
// @WebMvcTest doesn't set up controller method level validations:
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuditEventApiDelegateImplTest {

    private static final String BUSINESS_OBJECT_TYPE = "orders";
    private static final String SORT_BY = "type";
    private static final UUID BUSINESS_OBJECT_ID =
            UUID.fromString("1190241c-5eae-11ec-bf63-0242ac130002");
    private static final String LIST_EVENTS_PATH =
            "/api/v1/audit-events/orders/1190241c-5eae-11ec-bf63-0242ac130002";
    private static final String ASC = "ASC";

    @Autowired private MockMvc mockMvc;

    // This is to avoid loading the DB connection for the real repository class:
    @MockBean private AuditEventRepository auditEventRepository;

    @MockBean private AuditEventService auditEventService;

    @MockBean private AuthorizationHandler authorizationHandler;
    @MockBean private PublisherProperties publisherProperties;
    @MockBean private EventGateway eventGateway;

    @Mock private AuditEventMapper eventMapper;

    @BeforeEach
    void mockAuthorization() {
        ReflectionTestUtils.setField(auditEventService, "publisherProperties", publisherProperties);
        ReflectionTestUtils.setField(auditEventService, "eventGateway", eventGateway);

        when(authorizationHandler.isAllowed(any(), (Class<?>) any())).thenReturn(true);
        when(authorizationHandler.isAllowed(any(), (String) any())).thenReturn(true);
        when(authorizationHandler.isAllowedForInstance(any(), any())).thenReturn(true);
        when(authorizationHandler.getAuthFilter(any(), any())).thenReturn(element -> true);
    }

    @Test
    void getEvents() throws Exception {
        AuditEventEntity auditEvent = TestUtil.Data.STATE_CHANGE_ENTITY.readJson();
        Page<AuditEventEntity> page =
                new PageImpl<>(List.of(auditEvent), Pageable.ofSize(10).withPage(0), 20);
        // Expected result from PagingMetadataMapper.
        String nextPage =
                "http://localhost/api/v1/audit-events/orders/"
                        + "1190241c-5eae-11ec-bf63-0242ac130002?sortOrder=ASC&pageSize=10&sortBy=type"
                        + "&startTime=2021-12-02T20:00:28.570Z&endTime=2021-12-22T20:00:28.570Z&pageNumber=1";

        when(auditEventService.findAuditEvents(
                        BUSINESS_OBJECT_TYPE,
                        BUSINESS_OBJECT_ID,
                        OffsetDateTime.parse("2021-12-02T20:00:28.570Z"),
                        OffsetDateTime.parse("2021-12-22T20:00:28.570Z"),
                        0,
                        10,
                        ASC,
                        SORT_BY))
                .thenReturn(page);

        String urlTemplate =
                LIST_EVENTS_PATH
                        + "?sortOrder=ASC&sortBy=type&startTime=2021-12-02T20:00:28.570Z&"
                        + "endTime=2021-12-22T20:00:28.570Z&"
                        + "pageNumber=0&pageSize=10";
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].summary").value("sed ipsum in ex"))
                .andExpect(jsonPath("$.pagingMetadata.pageNumber").value(0))
                .andExpect(jsonPath("$.pagingMetadata.pageSize").value(10))
                .andExpect(jsonPath("$.pagingMetadata.totalCount").value(20))
                .andExpect(jsonPath("$.pagingMetadata.nextPage").value(nextPage));
    }

    @Test
    void getEvents_GivenInvalidTimeRange_ShouldReturnHttp400() throws Exception {
        when(auditEventService.findAuditEvents(
                        BUSINESS_OBJECT_TYPE,
                        BUSINESS_OBJECT_ID,
                        OffsetDateTime.parse("2021-12-02T20:00:28.571Z"),
                        OffsetDateTime.parse("2021-12-22T20:00:28.570Z"),
                        0,
                        10,
                        ASC,
                        SORT_BY))
                .thenThrow(ApiException.Builder.badRequest("ErRoR"));

        String urlTemplate =
                LIST_EVENTS_PATH
                        + "?sortOrder=ASC&sortBy=type&startTime=2021-12-02T20:00:28.571Z&"
                        + "endTime=2021-12-22T20:00:28.570Z&"
                        + "pageNumber=0&pageSize=10";
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isBadRequest())
                .andExpect(correctErrorMessages("ErRoR"));
    }

    @Test
    void getEvents_GivenTooLowPageNumber_ShouldReturnHttp400() throws Exception {
        String urlTemplate =
                LIST_EVENTS_PATH
                        + "?sortOrder=ASC&startTime=2021-12-02T20:00:28.571Z&endTime=2021-12-22T20:00:28.572Z&"
                        + "pageNumber=-1&pageSize=10";
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isBadRequest())
                .andExpect(correctErrorMessages("pageNumber: must be greater than or equal to 0"));
    }

    private static ResultMatcher correctErrorMessages(String... errorMessages) {
        return jsonPath("$.messages", hasItems(errorMessages));
    }

    @Test
    void getEvents_GivenTooLowPageSize_ShouldReturnHttp400() throws Exception {
        String urlTemplate =
                LIST_EVENTS_PATH
                        + "?sortOrder=ASC&startTime=2021-12-02T20:00:28.571Z&endTime=2021-12-22T20:00:28.572Z&"
                        + "pageNumber=0&pageSize=0";
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isBadRequest())
                .andExpect(correctErrorMessages("pageSize: must be greater than or equal to 1"));
    }

    @Test
    void getEvents_GivenTooHighPageSize_ShouldReturnHttp400() throws Exception {
        String urlTemplate =
                LIST_EVENTS_PATH
                        + "?sortOrder=ASC&startTime=2021-12-02T20:00:28.571Z&endTime=2021-12-22T20:00:28.572Z&"
                        + "pageNumber=0&pageSize=201";
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isBadRequest())
                .andExpect(correctErrorMessages("pageSize: must be less than or equal to 200"));
    }

    @Test
    void getEvents_ForbiddenAndNotProfileRequest() throws Exception {

        when(authorizationHandler.isAllowed("view", AuditEventEntity.class)).thenReturn(false);

        mockMvc.perform(get(LIST_EVENTS_PATH))
                .andExpect(status().isForbidden())
                .andExpect(correctErrorMessages("Forbidden request."));
    }

    @Test
    void getEvents_NotLinkedProfile() throws Exception {

        when(authorizationHandler.isAllowed("view", AuditEventEntity.class)).thenReturn(false);

        try (MockedStatic<SecurityContextUtility> mocked =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mocked.when(SecurityContextUtility::getAuthenticatedUserProfileLinks)
                    .thenReturn(
                            new HashSet<>(
                                    List.of(
                                            ProfileLink.builder()
                                                    .profileId(UUID.randomUUID())
                                                    .build())));

            mockMvc.perform(
                            get(
                                    "/api/v1/audit-events/employer/a190241c-5eae-11ec-bf63-0242ac130002"))
                    .andExpect(status().isForbidden())
                    .andExpect(correctErrorMessages("Forbidden request."));
        }
    }

    @Test
    void getEvents_LinkedToProfileButNotEnoughAccess() throws Exception {

        when(authorizationHandler.isAllowed("view", AuditEventEntity.class)).thenReturn(false);

        UUID wantedProfileId = UUID.randomUUID();
        ProfileLink wantedProfileLink =
                ProfileLink.builder()
                        .profileId(wantedProfileId)
                        .accessLevel(ProfileAccessLevel.WRITER.getValue())
                        .profileType(ProfileType.EMPLOYER.getValue())
                        .build();

        try (MockedStatic<SecurityContextUtility> mocked =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mocked.when(SecurityContextUtility::getAuthenticatedUserProfileLinks)
                    .thenReturn(
                            new HashSet<>(
                                    List.of(
                                            ProfileLink.builder()
                                                    .profileId(UUID.randomUUID())
                                                    .build(),
                                            wantedProfileLink)));

            mockMvc.perform(get("/api/v1/audit-events/employer/" + wantedProfileId.toString()))
                    .andExpect(status().isForbidden())
                    .andExpect(correctErrorMessages("Forbidden request."));
        }
    }

    @Test
    void getEvents_LinkedToProfileButUnexpectedProfileType() throws Exception {

        when(authorizationHandler.isAllowed("view", AuditEventEntity.class)).thenReturn(false);

        UUID wantedProfileId = UUID.randomUUID();
        ProfileLink wantedProfileLink =
                ProfileLink.builder()
                        .profileId(wantedProfileId)
                        .accessLevel(ProfileAccessLevel.ADMIN.getValue())
                        .profileType(ProfileType.EMPLOYER.getValue())
                        .build();

        try (MockedStatic<SecurityContextUtility> mocked =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mocked.when(SecurityContextUtility::getAuthenticatedUserProfileLinks)
                    .thenReturn(
                            new HashSet<>(
                                    List.of(
                                            ProfileLink.builder()
                                                    .profileId(UUID.randomUUID())
                                                    .build(),
                                            wantedProfileLink)));

            mockMvc.perform(get("/api/v1/audit-events/individual/" + wantedProfileId.toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            correctErrorMessages(
                                    "The specified profile is not of the expected type: individual"));
        }
    }

    @Test
    void getEvents_LinkedToProfileSuccess() throws Exception {
        AuditEventEntity auditEvent = TestUtil.Data.STATE_CHANGE_ENTITY.readJson();
        Page<AuditEventEntity> page =
                new PageImpl<>(List.of(auditEvent), Pageable.ofSize(10).withPage(0), 20);

        when(auditEventService.findAuditEvents(
                        "employer",
                        BUSINESS_OBJECT_ID,
                        OffsetDateTime.parse("2021-12-02T20:00:28.570Z"),
                        OffsetDateTime.parse("2021-12-22T20:00:28.570Z"),
                        0,
                        10,
                        ASC,
                        SORT_BY))
                .thenReturn(page);

        String urlTemplate =
                "/api/v1/audit-events/employer/"
                        + BUSINESS_OBJECT_ID
                        + "?sortOrder=ASC&sortBy=type&startTime=2021-12-02T20:00:28.570Z&"
                        + "endTime=2021-12-22T20:00:28.570Z&"
                        + "pageNumber=0&pageSize=10";

        when(authorizationHandler.isAllowed("view", AuditEventEntity.class)).thenReturn(false);

        ProfileLink wantedProfileLink =
                ProfileLink.builder()
                        .profileId(BUSINESS_OBJECT_ID)
                        .accessLevel(ProfileAccessLevel.ADMIN.getValue())
                        .profileType(ProfileType.EMPLOYER.getValue())
                        .build();

        ResultActions response;
        try (MockedStatic<SecurityContextUtility> mocked =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mocked.when(SecurityContextUtility::getAuthenticatedUserProfileLinks)
                    .thenReturn(
                            new HashSet<>(
                                    List.of(
                                            ProfileLink.builder()
                                                    .profileId(UUID.randomUUID())
                                                    .build(),
                                            wantedProfileLink)));

            response = mockMvc.perform(get(urlTemplate));
        }

        String expectedNextPage =
                "http://localhost/api/v1/audit-events/employer/"
                        + "1190241c-5eae-11ec-bf63-0242ac130002?sortOrder=ASC&pageSize=10&sortBy=type"
                        + "&startTime=2021-12-02T20:00:28.570Z&endTime=2021-12-22T20:00:28.570Z&pageNumber=1";

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].summary").value("sed ipsum in ex"))
                .andExpect(jsonPath("$.pagingMetadata.pageNumber").value(0))
                .andExpect(jsonPath("$.pagingMetadata.pageSize").value(10))
                .andExpect(jsonPath("$.pagingMetadata.totalCount").value(20))
                .andExpect(jsonPath("$.pagingMetadata.nextPage").value(expectedNextPage));
    }

    @Test
    void postEvent() throws Exception {
        AuditEventRequest body = TestUtil.Data.STATE_CHANGE_REQUEST.readJson();
        AuditEventEntity bodyData = TestUtil.Data.STATE_CHANGE_ENTITY.readJson();
        AuditEvent auditEvent = TestUtil.Data.STATE_CHANGE_EVENT.readJson();

        when(eventMapper.toAuditEvent(body, BUSINESS_OBJECT_ID, BUSINESS_OBJECT_TYPE))
                .thenReturn(auditEvent);
        doCallRealMethod().when(auditEventService).publishAuditEvent(any());
        when(publisherProperties.getFullyQualifiedTopicName(
                        PublisherTopic.AUDIT_EVENTS_RECORDING.name()))
                .thenReturn(Optional.of(PublisherTopic.AUDIT_EVENTS_RECORDING.name()));

        when(eventMapper.toEntity(auditEvent)).thenReturn(bodyData);

        mockMvc.perform(
                        post("/api/v1/audit-events/orders/1190241c-5eae-11ec-bf63-0242ac130002")
                                .content(TestUtil.Data.STATE_CHANGE_REQUEST.readJsonString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(eventGateway)
                .publishEvent(
                        any(AuditEvent.class), eq(PublisherTopic.AUDIT_EVENTS_RECORDING.name()));
    }

    @Test
    void postEvent_GivenInvalidRequestBody_ShouldReturnHttp400() throws Exception {
        mockMvc.perform(
                        post("/api/v1/audit-events/orders/1190241c-5eae-11ec-bf63-0242ac130002")
                                .content("{}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(
                        correctErrorMessages(
                                "'eventData': must not be null",
                                "'timestamp': must not be null",
                                "'summary': must not be null"));
    }
}
