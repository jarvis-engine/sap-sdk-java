package com.vengine.kk.sap.common.client;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.error.SapExceptionHandler;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Abstract base class for SAP ByDesign API clients.
 *
 * <p>Provides common HTTP operations (GET, POST) with:
 * <ul>
 *   <li>Automatic SAP response envelope unwrapping via {@link SapResponseDecoder}</li>
 *   <li>Typed exception handling via {@link SapExceptionHandler}</li>
 *   <li>MDC tracing ({@code sap.route}) for structured logging</li>
 *   <li>SLF4J debug logging for each outgoing request</li>
 * </ul>
 */
@Slf4j
public abstract class BaseSapClient {

    protected final RestTemplate restTemplate;
    protected final SapProperties properties;

    private final SapResponseDecoder decoder;
    private final SapExceptionHandler exceptionHandler;

    protected BaseSapClient(SapAuthenticatedClientFactory factory,
                            SapProperties properties,
                            SapResponseDecoder decoder,
                            SapExceptionHandler exceptionHandler) {
        this.restTemplate = factory.createClient();
        this.properties = properties;
        this.decoder = decoder;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Builds the full SAP URL for a given route.
     *
     * <p>URL pattern: {@code {baseUrl}/http/{env}/{route}}
     */
    protected String buildUrl(String route) {
        return properties.getBaseUrl() + "/http/" + properties.getEnv() + "/" + route;
    }

    /**
     * Performs a GET request and decodes a single entity from the SAP envelope.
     */
    protected <T> T get(String route, Class<T> responseType) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP GET {}", url);
            String body = doGet(url);
            return decoder.decode(body, responseType);
        } finally {
            MDC.remove("sap.route");
        }
    }

    /**
     * Performs a GET request and decodes a list of entities from the SAP envelope.
     */
    protected <T> List<T> getList(String route, Class<T> itemType) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP GET {}", url);
            String body = doGet(url);
            return decoder.decodeList(body, itemType);
        } finally {
            MDC.remove("sap.route");
        }
    }

    /**
     * Performs a POST request with a JSON body and decodes a single entity from the SAP envelope.
     */
    protected <T> T post(String route, Object body, Class<T> responseType) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP POST {}", url);
            String responseBody = doPost(url, body);
            return decoder.decode(responseBody, responseType);
        } finally {
            MDC.remove("sap.route");
        }
    }

    private String doGet(String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            exceptionHandler.handle(e.getResponseBodyAsString(), e.getStatusCode().value());
            return null; // unreachable — handle() always throws
        }
    }

    private String doPost(String url, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            exceptionHandler.handle(e.getResponseBodyAsString(), e.getStatusCode().value());
            return null; // unreachable — handle() always throws
        }
    }
}
