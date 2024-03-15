package io.nuvalence.platform.audit.service.config;

import io.nuvalence.events.brokerclient.config.PublisherProperties;
import io.nuvalence.events.brokerclient.config.SubscriberProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

/**
 * Allows recognition of beans in dsgov-events lib.
 */
@Configuration
@EnableIntegration
@ComponentScan(basePackages = "io.nuvalence.events")
@IntegrationComponentScan(
        basePackages = {"io.nuvalence.events.event.service", "io.nuvalence.platform.audit.service"})
@EnableConfigurationProperties({SubscriberProperties.class, PublisherProperties.class})
public class EventsConfig {}
