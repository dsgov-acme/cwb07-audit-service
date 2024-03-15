package io.nuvalence.platform.audit.service.utils;

import io.nuvalence.auth.token.SelfSignedTokenGenerator;
import io.nuvalence.auth.util.RsaKeyUtility;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Simple class that houses and provides the service to service token used in audit-service communication.
 */
@EnableScheduling
@Getter
@Component
public class ServiceTokenProvider {
    private String serviceToken;

    private final String privateKey;

    private final String issuer;

    private final List<String> roles;

    private final SelfSignedTokenGenerator tokenGenerator;

    /**
     * Initializes a new ServiceTokenProvider.
     *
     * @param privateKey RSA private key used to sign JWT
     * @param issuer JWT issuer
     * @param roles List of application role claims to include on JWT
     * @throws IOException If there is an error parsing the private key string.
     * @throws GeneralSecurityException If there is an error generating a JWT.
     */
    public ServiceTokenProvider(
            @Value("${auth.token-client.self-signed.private-key}") final String privateKey,
            @Value("${auth.token-client.self-signed.issuer}") final String issuer,
            @Value("${auth.token-client.self-signed.roles}") final List<String> roles)
            throws IOException, GeneralSecurityException {
        this.privateKey = privateKey;
        this.issuer = issuer;
        this.roles = roles;
        tokenGenerator =
                new SelfSignedTokenGenerator(
                        this.issuer,
                        Duration.ofMinutes(5),
                        RsaKeyUtility.getPrivateKeyFromString(this.privateKey));
        this.generateJwt();
    }

    /**
     * Generates a JWT for service to service auth on construction and every 40 mins after.
     */
    @Scheduled(fixedRate = 3, timeUnit = TimeUnit.MINUTES)
    protected final void generateJwt() {
        this.serviceToken = tokenGenerator.generateToken("audit-service", roles);
    }
}
