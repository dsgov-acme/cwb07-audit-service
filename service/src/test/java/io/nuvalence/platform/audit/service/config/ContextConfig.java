package io.nuvalence.platform.audit.service.config;

import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Test configuration for the app test context.
 */
@TestConfiguration
public class ContextConfig {
    @MockBean private PubSubAdmin pubSubAdmin;
    @MockBean private PubSubTemplate pubSubTemplate;
}
