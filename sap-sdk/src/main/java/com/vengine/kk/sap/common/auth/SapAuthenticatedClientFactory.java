package com.vengine.kk.sap.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.error.SapResponseErrorHandler;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Creates a pre-configured {@link RestTemplate} with the appropriate
 * authentication interceptor (BASIC or OAUTH2), standard timeouts,
 * and the SAP-specific {@link SapResponseErrorHandler} for HTTP-level errors.
 */
@Component
public class SapAuthenticatedClientFactory {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS    = 30_000;

    private final SapProperties properties;
    private final ObjectMapper objectMapper;

    public SapAuthenticatedClientFactory(SapProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public RestTemplate createClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);

        RestTemplate restTemplate = new RestTemplate(factory);

        ClientHttpRequestInterceptor interceptor;
        switch (properties.getCredentials().getAuthType()) {
            case OAUTH2:
                interceptor = new OAuth2BearerTokenInterceptor(properties);
                break;
            case BASIC:
            default:
                interceptor = new BasicAuthInterceptor(properties);
                break;
        }

        restTemplate.setInterceptors(List.of(interceptor));
        restTemplate.setErrorHandler(new SapResponseErrorHandler(objectMapper));
        return restTemplate;
    }
}
