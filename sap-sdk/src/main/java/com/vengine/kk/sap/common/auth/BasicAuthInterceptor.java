package com.vengine.kk.sap.common.auth;

import com.vengine.kk.sap.common.config.SapProperties;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Adds Basic-Auth and {@code Origin-Project} header to every outgoing request.
 *
 * <p>The PHP SAP SDK (venginetech/sap-sdk) sends {@code Origin-Project} as a plain
 * string value — e.g. {@code "ep"} or {@code "eshop"}. No encryption is applied.
 * This implementation matches that contract exactly.
 *
 * <p>Valid values are: {@code ep}, {@code eshop} (or null/blank to omit the header).
 */
public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {

    private static final String HEADER_AUTHORIZATION  = "Authorization";
    private static final String HEADER_ORIGIN_PROJECT = "Origin-Project";

    private final String basicCredentials;

    @Nullable
    private final String originProject;

    public BasicAuthInterceptor(SapProperties properties) {
        SapProperties.Credentials credentials = properties.getCredentials();
        String raw = credentials.getUsername() + ":" + credentials.getPassword();
        this.basicCredentials = Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        this.originProject = properties.getOriginProject();
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        request.getHeaders().set(HEADER_AUTHORIZATION, "Basic " + basicCredentials);

        if (originProject != null && !originProject.isBlank()) {
            request.getHeaders().set(HEADER_ORIGIN_PROJECT, originProject);
        }

        return execution.execute(request, body);
    }
}
