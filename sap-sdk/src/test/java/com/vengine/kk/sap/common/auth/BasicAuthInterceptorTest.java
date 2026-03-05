package com.vengine.kk.sap.common.auth;

import com.vengine.kk.sap.common.config.SapProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BasicAuthInterceptor}.
 *
 * <p>The PHP SAP SDK sends {@code Origin-Project} as a plain string (no encryption).
 * These tests verify the Java implementation matches that contract exactly.
 */
@ExtendWith(MockitoExtension.class)
class BasicAuthInterceptorTest {

    @Mock
    private ClientHttpRequestExecution execution;

    private SapProperties createProperties(String username, String password, String originProject) {
        SapProperties props = new SapProperties();
        SapProperties.Credentials creds = new SapProperties.Credentials();
        creds.setUsername(username);
        creds.setPassword(password);
        props.setCredentials(creds);
        props.setOriginProject(originProject);
        return props;
    }

    @Test
    void authorizationHeaderIsBasicBase64() throws Exception {
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(createProperties("myuser", "mypass", null));
        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        String expected = Base64.getEncoder().encodeToString("myuser:mypass".getBytes(StandardCharsets.UTF_8));
        assertThat(request.getHeaders().getFirst("Authorization")).isEqualTo("Basic " + expected);
    }

    @Test
    void originProjectHeaderAbsentWhenOriginProjectIsNull() throws Exception {
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(createProperties("user", "pass", null));
        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().containsKey("Origin-Project")).isFalse();
    }

    @Test
    void originProjectHeaderAbsentWhenOriginProjectIsBlank() throws Exception {
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(createProperties("user", "pass", "  "));
        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().containsKey("Origin-Project")).isFalse();
    }

    @Test
    void originProjectHeaderSentAsPlainString() throws Exception {
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(createProperties("user", "pass", "ep"));
        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().getFirst("Origin-Project")).isEqualTo("ep");
    }

    @Test
    void originProjectHeaderSentAsPlainStringForEshop() throws Exception {
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(createProperties("user", "pass", "eshop"));
        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().getFirst("Origin-Project")).isEqualTo("eshop");
    }

    @Test
    void noXOriginProjectHeaderSentAnywhere() throws Exception {
        // Verify the old (wrong) header name is never sent
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(createProperties("user", "pass", "ep"));
        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().containsKey("X-Origin-Project")).isFalse();
    }
}
