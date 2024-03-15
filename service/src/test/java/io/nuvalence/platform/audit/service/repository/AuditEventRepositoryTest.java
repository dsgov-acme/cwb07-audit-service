package io.nuvalence.platform.audit.service.repository;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.platform.audit.service.domain.ActivityEventEntity;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.domain.enums.TypeEnum;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize"})
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditEventRepositoryTest {
    private static final String businessObjectType = "test";
    private static final PageRequest pageRequest = PageRequest.of(0, 10);
    private final OffsetDateTime rangeStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final UUID firstObjectId = UUID.randomUUID();
    private final UUID secondObjectId = UUID.randomUUID();
    @Autowired private AuditEventRepository auditEventRepository;

    @MockBean private AuthorizationHandler authorizationHandler;

    @BeforeAll
    public void beforeEach() {
        System.out.println(rangeStart);
        // seed data -- an event every hour for each entity
        List<ActivityEventEntity> eventsWithoutEntityId =
                IntStream.range(0, 5)
                        .mapToObj(
                                h -> {
                                    ActivityEventEntity record = new ActivityEventEntity();
                                    record.setTimestamp(rangeStart.plusHours(h));
                                    record.setEventId(UUID.randomUUID());
                                    record.setBusinessObjectType(businessObjectType);
                                    record.setType(TypeEnum.ACTIVITY_EVENT_DATA);
                                    record.setActivityType("entry-" + h);
                                    record.setData("{\"hour\": \"" + h + "\"}");
                                    return record;
                                })
                        .collect(Collectors.toList());

        eventsWithoutEntityId.forEach(
                e -> {
                    e.setBusinessObjectId(firstObjectId);
                    auditEventRepository.save(e);
                    e.setBusinessObjectId(secondObjectId);
                    auditEventRepository.save(e);
                });
    }

    @Test
    void findAll_givenFirstObjectIdAndNoTimeRange_ShouldReturnAllEventsForFirstObject() {
        var items =
                auditEventRepository
                        .findAll(businessObjectType, firstObjectId, null, null, pageRequest)
                        .getContent();

        debugItems(items);
        MatcherAssert.assertThat(
                items, hasNItemsMatching(5, businessObjectIdMatches(firstObjectId)));
    }

    @Test
    void
            findAll_givenFirstObjectIdAndStartTime_ShouldReturnAllEventsForFirstObjectAfterStartTime() {
        System.out.println(rangeStart);
        var queryStart = rangeStart.plusHours(1);
        var items =
                auditEventRepository
                        .findAll(businessObjectType, firstObjectId, queryStart, null, pageRequest)
                        .getContent();

        var matcher =
                Matchers.both(businessObjectIdMatches(firstObjectId))
                        .and(timestampMatches(Matchers.greaterThanOrEqualTo(queryStart)));

        debugItems(items);
        MatcherAssert.assertThat(items, hasNItemsMatching(4, matcher));
    }

    @Test
    void findAll_givenFirstObjectIdAndEndTime_ShouldReturnAllEventsForFirstObjectBeforeEndTime() {
        System.out.println(rangeStart);
        var queryEnd = rangeStart.plusHours(4);
        var items =
                auditEventRepository
                        .findAll(businessObjectType, firstObjectId, null, queryEnd, pageRequest)
                        .getContent();

        var matcher =
                Matchers.both(businessObjectIdMatches(firstObjectId))
                        .and(timestampMatches(Matchers.lessThan(queryEnd)));

        debugItems(items);
        MatcherAssert.assertThat(items, hasNItemsMatching(4, matcher));
    }

    @Test
    void
            findAll_givenFirstObjectIdStartTimeAndEndTime_ShouldReturnAllEventsForFirstObjectInRange() {
        System.out.println(rangeStart);
        var queryStart = rangeStart.plusHours(1);
        var queryEnd = rangeStart.plusHours(4);
        var items =
                auditEventRepository
                        .findAll(
                                businessObjectType,
                                firstObjectId,
                                queryStart,
                                queryEnd,
                                pageRequest)
                        .getContent();

        var matcher =
                Matchers.both(businessObjectIdMatches(firstObjectId))
                        .and(
                                timestampMatches(
                                        Matchers.both(Matchers.lessThan(queryEnd))
                                                .and(Matchers.greaterThanOrEqualTo(queryStart))));

        debugItems(items);
        MatcherAssert.assertThat(items, hasNItemsMatching(3, matcher));
    }

    private Matcher<List<AuditEventEntity>> hasNItemsMatching(
            int expectedSize, Matcher<AuditEventEntity> matcher) {
        return Matchers.<List<AuditEventEntity>>both(Matchers.everyItem(matcher))
                .and(Matchers.hasSize(expectedSize));
    }

    private Matcher<AuditEventEntity> businessObjectIdMatches(UUID expected) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(AuditEventEntity auditEventEntity) {
                return expected.equals(auditEventEntity.getBusinessObjectId());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should have entity id " + expected);
            }
        };
    }

    private Matcher<AuditEventEntity> timestampMatches(Matcher<OffsetDateTime> matcher) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(AuditEventEntity auditEventEntity) {
                return matcher.matches(auditEventEntity.getTimestamp());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should have matching timestamp");
                matcher.describeTo(description);
            }
        };
    }

    private void debugItems(List<AuditEventEntity> items) {
        System.out.println("*******************");
        System.out.println("size =>" + items.size());
        items.forEach(
                i -> {
                    System.out.println("id =>" + i.getEventId());
                    System.out.println("timestamp =>" + i.getTimestamp());
                    System.out.println("BusinessObjectId =>" + i.getBusinessObjectId());
                    System.out.println("------------------");
                });
        System.out.println("*******************");
    }
}
