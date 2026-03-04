package com.vengine.kk.sap.autoconfigure;

import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.config.SapRoutes;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableConfigurationProperties(SapProperties.class)
@ComponentScan(basePackages = "com.vengine.kk.sap")
public class SapAutoConfiguration {

    @Bean
    public SapRoutes sapRoutes(SapProperties properties) {
        return new SapRoutes(properties);
    }
}
