package com.vengine.kk.sap.common.auth;

import com.vengine.kk.sap.common.exception.SapClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2BearerTokenInterceptorTest {

    @Mock
    private RestTemplate tokenRestTemplate;

    @Mock
    private ClientHttpRequestExecution execution;

    private OAuth2BearerTokenInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new OAuth2BearerTokenInterceptor(
                "https://auth.example.com/token", "client-id", "client-secret", tokenRestTemplate);
    }

    private void stubTokenResponse(String accessToken, long expiresIn) {
        // Build a JSON map that Jackson would produce for the TokenResponse
        // Since TokenResponse is private, we mock the RestTemplate exchange call
        @SuppressWarnings("unchecked")
        ResponseEntity<Object> response = mock(ResponseEntity.class);

        // We need to use a real map-based approach since TokenResponse is private
        // Instead, let's use the actual exchange mock
        // The interceptor calls tokenRestTemplate.exchange(tokenUrl, POST, entity, TokenResponse.class)
        // We'll use Answer to return a proper ResponseEntity with a body
        when(tokenRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class)))
                .thenAnswer(invocation -> {
                    // Create a real token response via reflection
                    Class<?> tokenResponseClass = Class.forName(
                            "com.vengine.kk.sap.common.auth.OAuth2BearerTokenInterceptor$TokenResponse");
                    Object tokenResponse = tokenResponseClass.getDeclaredConstructor().newInstance();

                    var setAccessToken = tokenResponseClass.getDeclaredMethod("setAccessToken", String.class);
                    setAccessToken.setAccessible(true);
                    setAccessToken.invoke(tokenResponse, accessToken);

                    var setExpiresIn = tokenResponseClass.getDeclaredMethod("setExpiresIn", long.class);
                    setExpiresIn.setAccessible(true);
                    setExpiresIn.invoke(tokenResponse, expiresIn);

                    return ResponseEntity.ok(tokenResponse);
                });
    }

    @Test
    void tokenIsFetchedOnFirstRequestAndCached() throws Exception {
        stubTokenResponse("token-abc", 3600);

        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token-abc");
        verify(tokenRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), any(Class.class));
    }

    @Test
    void cachedTokenIsReusedOnSecondRequest() throws Exception {
        stubTokenResponse("token-abc", 3600);

        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        MockClientHttpRequest request1 = new MockClientHttpRequest();
        interceptor.intercept(request1, new byte[0], execution);

        MockClientHttpRequest request2 = new MockClientHttpRequest();
        interceptor.intercept(request2, new byte[0], execution);

        assertThat(request2.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token-abc");
        // Only one call to fetch token
        verify(tokenRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), any(Class.class));
    }

    @Test
    void tokenRefreshHappensWhenExpired() throws Exception {
        stubTokenResponse("token-1", 3600);
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        // First call to cache the token
        interceptor.intercept(new MockClientHttpRequest(), new byte[0], execution);

        // Expire the token by setting expiresAt to the past via reflection
        setExpiresAt(interceptor, System.currentTimeMillis() - 1000);

        // Re-stub to return a new token
        stubTokenResponse("token-2", 3600);

        MockClientHttpRequest request = new MockClientHttpRequest();
        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token-2");
        verify(tokenRestTemplate, times(2)).exchange(anyString(), eq(HttpMethod.POST), any(), any(Class.class));
    }

    @Test
    void tokenRefreshHappensWithin30sBufferOfExpiry() throws Exception {
        stubTokenResponse("token-1", 3600);
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(new MockClientHttpRequest(), new byte[0], execution);

        // Set expiresAt to 20 seconds from now (within 30s buffer)
        setExpiresAt(interceptor, System.currentTimeMillis() + 20_000);

        stubTokenResponse("token-refreshed", 3600);

        MockClientHttpRequest request = new MockClientHttpRequest();
        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token-refreshed");
        verify(tokenRestTemplate, times(2)).exchange(anyString(), eq(HttpMethod.POST), any(), any(Class.class));
    }

    @Test
    void tokenIsNotRefreshedWhenStillValidWithMoreThan30sRemaining() throws Exception {
        stubTokenResponse("token-valid", 3600);
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(new MockClientHttpRequest(), new byte[0], execution);

        // expiresAt is ~3600s from now, well beyond 30s buffer — should NOT refresh
        MockClientHttpRequest request = new MockClientHttpRequest();
        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token-valid");
        verify(tokenRestTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), any(Class.class));
    }

    @Test
    void failedTokenFetchThrowsSapClientException() {
        when(tokenRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(Class.class)))
                .thenThrow(new RestClientException("Connection refused"));

        MockClientHttpRequest request = new MockClientHttpRequest();
        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], execution))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("Failed to acquire SAP OAuth2 token");
    }

    @Test
    void concurrentAccessFetchesTokenOnlyOnce() throws Exception {
        AtomicInteger fetchCount = new AtomicInteger(0);

        when(tokenRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class)))
                .thenAnswer(invocation -> {
                    fetchCount.incrementAndGet();
                    Thread.sleep(50); // Simulate slow token fetch

                    Class<?> tokenResponseClass = Class.forName(
                            "com.vengine.kk.sap.common.auth.OAuth2BearerTokenInterceptor$TokenResponse");
                    Object tokenResponse = tokenResponseClass.getDeclaredConstructor().newInstance();

                    var setAccessToken = tokenResponseClass.getDeclaredMethod("setAccessToken", String.class);
                    setAccessToken.setAccessible(true);
                    setAccessToken.invoke(tokenResponse, "concurrent-token");

                    var setExpiresIn = tokenResponseClass.getDeclaredMethod("setExpiresIn", long.class);
                    setExpiresIn.setAccessible(true);
                    setExpiresIn.invoke(tokenResponse, 3600L);

                    return ResponseEntity.ok(tokenResponse);
                });

        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    interceptor.intercept(new MockClientHttpRequest(), new byte[0], execution);
                } catch (Exception e) {
                    // ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Token should have been fetched exactly once due to synchronization
        assertThat(fetchCount.get()).isEqualTo(1);
    }

    private void setExpiresAt(Object target, long value) throws Exception {
        Field field = target.getClass().getDeclaredField("expiresAt");
        field.setAccessible(true);
        field.setLong(target, value);
    }
}
