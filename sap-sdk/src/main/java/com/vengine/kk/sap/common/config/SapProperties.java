package com.vengine.kk.sap.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sap")
@Data
public class SapProperties {

    private String baseUrl;
    private String env;
    private String encryptKey;
    private String originProject;
    private Credentials credentials = new Credentials();
    private Features features = new Features();

    @Data
    public static class Credentials {
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
