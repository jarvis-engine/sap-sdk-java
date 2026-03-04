package com.vengine.kk.sap.common.auth;

import com.vengine.kk.sap.common.config.SapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Adds Basic-Auth to every outgoing request.
 * Optionally encrypts the {@code originProject} value and sends it as
 * the {@code X-Origin-Project} header (AES-256-CTR + HMAC-SHA256).
 */
@Slf4j
public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {

    private static final String HEADER_AUTHORIZATION    = "Authorization";
    private static final String HEADER_ORIGIN_PROJECT   = "X-Origin-Project";
    private static final int    IV_LENGTH_BYTES         = 16;
    private static final int    KEY_LENGTH_BYTES        = 32; // 256-bit

    private final String basicCredentials;

    @Nullable
    private final String originProject;

    @Nullable
    private final byte[] encryptKeyBytes;

    public BasicAuthInterceptor(SapProperties properties) {
        SapProperties.Credentials credentials = properties.getCredentials();
        String raw = credentials.getUsername() + ":" + credentials.getPassword();
        this.basicCredentials = Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        this.originProject = properties.getOriginProject();

        String encryptKey = properties.getEncryptKey();
        if (encryptKey != null && !encryptKey.isBlank()) {
            byte[] keyBytes = encryptKey.getBytes(StandardCharsets.UTF_8);
            this.encryptKeyBytes = padOrTruncate(keyBytes, KEY_LENGTH_BYTES);
        } else {
            this.encryptKeyBytes = null;
        }
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        request.getHeaders().set(HEADER_AUTHORIZATION, "Basic " + basicCredentials);

        if (originProject != null && !originProject.isBlank() && encryptKeyBytes != null) {
            try {
                String encrypted = encryptOriginProject(originProject);
                request.getHeaders().set(HEADER_ORIGIN_PROJECT, encrypted);
            } catch (Exception e) {
                log.warn("Failed to encrypt originProject header, skipping", e);
            }
        }

        return execution.execute(request, body);
    }

    /**
     * Encrypts the value using AES-256-CTR and then signs the ciphertext with HMAC-SHA256.
     * Output format (Base64): IV (16 bytes) || HMAC (32 bytes) || ciphertext
     */
    private String encryptOriginProject(String value) throws Exception {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(encryptKeyBytes, "AES"),
                new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(encryptKeyBytes, "HmacSHA256"));
        mac.update(iv);
        byte[] hmac = mac.doFinal(ciphertext);

        // Concatenate: iv || hmac || ciphertext
        byte[] result = new byte[IV_LENGTH_BYTES + hmac.length + ciphertext.length];
        System.arraycopy(iv,         0, result, 0,                                   IV_LENGTH_BYTES);
        System.arraycopy(hmac,       0, result, IV_LENGTH_BYTES,                     hmac.length);
        System.arraycopy(ciphertext, 0, result, IV_LENGTH_BYTES + hmac.length,       ciphertext.length);

        return Base64.getEncoder().encodeToString(result);
    }

    private static byte[] padOrTruncate(byte[] input, int length) {
        byte[] result = new byte[length];
        System.arraycopy(input, 0, result, 0, Math.min(input.length, length));
        return result;
    }
}
