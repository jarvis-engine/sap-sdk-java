package com.vengine.kk.sap.common.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "sap")
@Validated
@Data
public class SapProperties {

    @NotBlank(message = "sap.base-url must not be blank")
    private String baseUrl;

    @NotBlank(message = "sap.env must not be blank")
    private String env;

    private String originProject;
    private Credentials credentials = new Credentials();
    private Features features = new Features();

    public enum AuthType {
        BASIC, OAUTH2
    }

    @Data
    public static class Credentials {
        private AuthType authType = AuthType.BASIC;
        private String oauthClientId;
        private String oauthClientSecret;
        private String oauthTokenUrl;
        private String username;
        private String password;
    }

    @Data
    public static class Features {
        private boolean salesOrderV3EndpointEnabled;
        private boolean salesOrderV4EndpointEnabled;
        private boolean salesOrderV5EndpointEnabled;
        private boolean customerV2EndpointEnabled;
        private boolean packageConfigurationV2EndpointEnabled;
        private boolean productAvailabilityV2EndpointEnabled;
    }
}
