package io.nuvalence.platform.audit.utils.cucumber.transformers;

import io.nuvalence.platform.audit.client.generated.models.ActivityEventData;

import java.util.Map;

/**
 * Transforms a data table row to an activity event request.
 */
public class ActivityEventTableTransformer
        extends AbstractAuditEventTableTransformer<ActivityEventData> {

    private static final String ACTIVITY_EVENT_DATA = "ActivityEventData";

    @Override
    public ActivityEventData asEventData(Map<String, String> item) {
        var eventData = new ActivityEventData().data(item.get("data"));
        eventData.setType(ACTIVITY_EVENT_DATA);
        eventData.setActivityType(item.get("type"));
        return eventData;
    }
}
