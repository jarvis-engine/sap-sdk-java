package com.vengine.kk.sap.common.auth;

import com.vengine.kk.sap.common.config.SapProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicAuthInterceptorTest {

    @Mock
    private ClientHttpRequestExecution execution;

    private SapProperties createProperties(String username, String password,
                                            String encryptKey, String originProject) {
        SapProperties props = new SapProperties();
        SapProperties.Credentials creds = new SapProperties.Credentials();
        creds.setUsername(username);
        creds.setPassword(password);
        props.setCredentials(creds);
        props.setEncryptKey(encryptKey);
        props.setOriginProject(originProject);
        return props;
    }

    @Test
    void authorizationHeaderIsBasicBase64() throws Exception {
        SapProperties props = createProperties("myuser", "mypass", null, null);
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(props);

        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        String expected = Base64.getEncoder().encodeToString("myuser:mypass".getBytes(StandardCharsets.UTF_8));
        assertThat(request.getHeaders().getFirst("Authorization")).isEqualTo("Basic " + expected);
    }

    @Test
    void originProjectHeaderAbsentWhenEncryptKeyIsNull() throws Exception {
        SapProperties props = createProperties("user", "pass", null, "my-project");
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(props);

        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().containsKey("X-Origin-Project")).isFalse();
    }

    @Test
    void originProjectHeaderAbsentWhenEncryptKeyIsBlank() throws Exception {
        SapProperties props = createProperties("user", "pass", "   ", "my-project");
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(props);

        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().containsKey("X-Origin-Project")).isFalse();
    }

    @Test
    void originProjectHeaderIsSetWhenEncryptKeyProvided() throws Exception {
        SapProperties props = createProperties("user", "pass", "my-secret-key-1234567890abcdef", "my-project");
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(props);

        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(request.getHeaders().getFirst("X-Origin-Project")).isNotNull().isNotBlank();
    }

    @Test
    void originProjectValueHasCorrectByteStructure() throws Exception {
        SapProperties props = createProperties("user", "pass", "my-secret-key-1234567890abcdef", "my-project");
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(props);

        MockClientHttpRequest request = new MockClientHttpRequest();
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(request, new byte[0], execution);

        String headerValue = request.getHeaders().getFirst("X-Origin-Project");
        byte[] decoded = Base64.getDecoder().decode(headerValue);

        // IV[16] + HMAC[32] + ciphertext[len(plaintext)]
        int expectedPlaintextLen = "my-project".getBytes(StandardCharsets.UTF_8).length;
        assertThat(decoded.length).isEqualTo(16 + 32 + expectedPlaintextLen);
    }

    @Test
    void twoCallsWithSamePlaintextProduceDifferentCiphertext() throws Exception {
        SapProperties props = createProperties("user", "pass", "my-secret-key-1234567890abcdef", "my-project");
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(props);

        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        MockClientHttpRequest request1 = new MockClientHttpRequest();
        interceptor.intercept(request1, new byte[0], execution);

        MockClientHttpRequest request2 = new MockClientHttpRequest();
        interceptor.intercept(request2, new byte[0], execution);

        String header1 = request1.getHeaders().getFirst("X-Origin-Project");
        String header2 = request2.getHeaders().getFirst("X-Origin-Project");

        // Due to random IV, the values should differ
        assertThat(header1).isNotEqualTo(header2);
    }

    @Test
    void encryptedOutputCanBeDecryptedBackToOriginalPlaintext() throws Exception {
        String key = "my-secret-key-1234567890abcdef";
        String plaintext = "my-project";
        SapProperties props = createProperties("user", "pass", key, plaintext);
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(props);

        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        MockClientHttpRequest request = new MockClientHttpRequest();
        interceptor.intercept(request, new byte[0], execution);

        String headerValue = request.getHeaders().getFirst("X-Origin-Project");
        byte[] decoded = Base64.getDecoder().decode(headerValue);

        // Extract IV, HMAC, ciphertext
        byte[] iv = new byte[16];
        byte[] hmac = new byte[32];
        byte[] ciphertext = new byte[decoded.length - 48];
        System.arraycopy(decoded, 0, iv, 0, 16);
        System.arraycopy(decoded, 16, hmac, 0, 32);
        System.arraycopy(decoded, 48, ciphertext, 0, ciphertext.length);

        // Prepare the key (padded/truncated to 32 bytes, same as BasicAuthInterceptor)
        byte[] keyBytes = new byte[32];
        byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(rawKey, 0, keyBytes, 0, Math.min(rawKey.length, 32));

        // Verify HMAC
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        mac.update(iv);
        byte[] expectedHmac = mac.doFinal(ciphertext);
        assertThat(hmac).isEqualTo(expectedHmac);

        // Decrypt
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(ciphertext);

        assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo(plaintext);
    }
}
