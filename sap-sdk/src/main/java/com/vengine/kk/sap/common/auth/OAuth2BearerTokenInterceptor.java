package com.vengine.kk.sap.common.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.exception.SapClientException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * OAuth2 client-credentials interceptor with thread-safe token caching.
 * Fetches a bearer token from the configured token URL and caches it,
 * refreshing 30 seconds before expiry.
 */
@Slf4j
public class OAuth2BearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final long EXPIRY_BUFFER_MS = 30_000;

    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final RestTemplate tokenRestTemplate;

    private String cachedToken;
    private long expiresAt;

    public OAuth2BearerTokenInterceptor(SapProperties properties) {
        SapProperties.Credentials credentials = properties.getCredentials();
        this.tokenUrl = credentials.getOauthTokenUrl();
        this.clientId = credentials.getOauthClientId();
        this.clientSecret = credentials.getOauthClientSecret();
        this.tokenRestTemplate = new RestTemplate();
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + getToken());
        return execution.execute(request, body);
    }

    private synchronized String getToken() {
        if (cachedToken == null || System.currentTimeMillis() > expiresAt - EXPIRY_BUFFER_MS) {
            fetchToken();
        }
        return cachedToken;
    }

    private void fetchToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            ResponseEntity<TokenResponse> response = tokenRestTemplate.exchange(
                    tokenUrl, HttpMethod.POST, entity, TokenResponse.class);

            TokenResponse tokenResponse = response.getBody();
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                throw new SapClientException("Failed to acquire SAP OAuth2 token");
            }

            this.cachedToken = tokenResponse.getAccessToken();
            this.expiresAt = System.currentTimeMillis() + (tokenResponse.getExpiresIn() * 1000);

            log.info("SAP OAuth2 token acquired, expires in {}s", tokenResponse.getExpiresIn());
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SapClientException("Failed to acquire SAP OAuth2 token", e);
        }
    }

    @Data
    private static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private long expiresIn;
    }
}
