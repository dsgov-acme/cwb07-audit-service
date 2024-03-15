package io.nuvalence.platform.audit.utils.cucumber.contexts;

import io.nuvalence.platform.audit.client.ApiException;
import io.nuvalence.platform.audit.client.ApiResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Wraps API Calls and holds most recent response data.
 */
@Getter
@Setter
public class ApiClientContext {
    private int lastResponseStatus;
    private Map<String, List<String>> lastResponseHeaders;
    private Object lastResponseBody;

    /**
     * Executes the API Call and captures response data.
     *
     * @param apiCall call to execute
     */
    public void capture(final ApiCall apiCall) {
        try {
            final ApiResponse<?> response = apiCall.call();
            lastResponseStatus = response.getStatusCode();
            lastResponseHeaders = response.getHeaders();
            lastResponseBody = response.getData();
        } catch (ApiException e) {
            lastResponseStatus = e.getCode();
            lastResponseHeaders = e.getResponseHeaders().map();
            lastResponseBody = e.getResponseBody();
        }
    }

    public <T> T getLastResponseBody(final Class<T> type) {
        return type.cast(lastResponseBody);
    }

    /**
     * Functional interface that wraps API calls.
     */
    @FunctionalInterface
    public interface ApiCall {
        ApiResponse<?> call() throws ApiException;
    }
}
