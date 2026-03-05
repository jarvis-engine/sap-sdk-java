package com.vengine.kk.sap.common.client;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.exception.SapClientException;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
public abstract class BaseSapClient {

    protected final SapProperties properties;
    private final RestTemplate restTemplate;
    private final SapResponseDecoder decoder;

    protected BaseSapClient(SapAuthenticatedClientFactory factory,
                             SapProperties properties,
                             SapResponseDecoder decoder) {
        this.restTemplate = factory.createClient();
        this.properties   = properties;
        this.decoder      = decoder;
    }

    protected <T> T get(String route, Class<T> responseType) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP GET {}", url);
            String body = restTemplate.getForObject(url, String.class);
            return decoder.decode(body, responseType);
        } catch (ResourceAccessException e) {
            throw new SapClientException("SAP connection error: " + e.getMessage(), e);
        } finally {
            MDC.remove("sap.route");
        }
    }

    protected <T> List<T> getList(String route, Class<T> itemType) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP GET {}", url);
            String body = restTemplate.getForObject(url, String.class);
            return decoder.decodeList(body, itemType);
        } catch (ResourceAccessException e) {
            throw new SapClientException("SAP connection error: " + e.getMessage(), e);
        } finally {
            MDC.remove("sap.route");
        }
    }

    protected <T> T post(String route, Object body, Class<T> responseType) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP POST {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return decoder.decode(response.getBody(), responseType);
        } catch (ResourceAccessException e) {
            throw new SapClientException("SAP connection error: " + e.getMessage(), e);
        } finally {
            MDC.remove("sap.route");
        }
    }

    protected <T> List<T> postList(String route, Object body, Class<T> itemType) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP POST (list) {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return decoder.decodeList(response.getBody(), itemType);
        } catch (ResourceAccessException e) {
            throw new SapClientException("SAP connection error: " + e.getMessage(), e);
        } finally {
            MDC.remove("sap.route");
        }
    }

    protected void postVoid(String route, Object body) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP POST (void) {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (ResourceAccessException e) {
            throw new SapClientException("SAP connection error: " + e.getMessage(), e);
        } finally {
            MDC.remove("sap.route");
        }
    }

    protected void delete(String route) {
        String url = buildUrl(route);
        MDC.put("sap.route", route);
        try {
            log.debug("SAP DELETE {}", url);
            restTemplate.delete(url);
        } catch (ResourceAccessException e) {
            throw new SapClientException("SAP connection error: " + e.getMessage(), e);
        } finally {
            MDC.remove("sap.route");
        }
    }

    protected String buildUrl(String route) {
        return properties.getBaseUrl() + "/http/" + properties.getEnv() + "/" + route;
    }

    protected String appendQueryParams(String route, Map<String, ?> params) {
        if (params == null || params.isEmpty()) return route;
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((k, v) -> {
            if (v != null && !v.toString().isEmpty()) {
                joiner.add(k + "=" + URLEncoder.encode(v.toString(), StandardCharsets.UTF_8));
            }
        });
        String query = joiner.toString();
        return query.isEmpty() ? route : route + "?" + query;
    }
}
