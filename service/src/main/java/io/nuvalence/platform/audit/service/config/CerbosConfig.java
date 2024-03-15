package io.nuvalence.platform.audit.service.config;

import dev.cerbos.sdk.CerbosBlockingClient;
import dev.cerbos.sdk.CerbosClientBuilder;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.auth.access.cerbos.CerbosAuthorizationHandler;
import io.nuvalence.auth.token.profiles.rest.RestUserFetchingStrategy;
import io.nuvalence.platform.audit.service.utils.JacocoIgnoreInGeneratedReport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;

/**
 * Configures CerbosAuthorizationHandler.
 */
@Configuration
@RequiredArgsConstructor
@Profile("!test")
@JacocoIgnoreInGeneratedReport(
        reason =
                "Initialization has side effects making unit tests difficult. Tested in acceptance tests.")
public class CerbosConfig {

    @Value("${cerbos.uri}")
    private String cerbosUri;

    @Value("${userManagement.baseUrl}")
    private String userManagementBaseUrl;

    /**
     * Initializes a CerbosAuthorizationHandler as a singleton bean.
     *
     * @return AuthorizationHandler
     * @throws CerbosClientBuilder.InvalidClientConfigurationException if cerbos URI is invalid
     */
    @Bean
    @Scope("singleton")
    public AuthorizationHandler getAuthorizationHandler()
            throws CerbosClientBuilder.InvalidClientConfigurationException {
        final CerbosBlockingClient cerbosClient =
                new CerbosClientBuilder(cerbosUri).withPlaintext().buildBlockingClient();

        return new CerbosAuthorizationHandler(cerbosClient);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RestUserFetchingStrategy getRestUserFetchingStrategy(RestTemplate restTemplate) {
        return new RestUserFetchingStrategy(restTemplate, userManagementBaseUrl);
    }
}
