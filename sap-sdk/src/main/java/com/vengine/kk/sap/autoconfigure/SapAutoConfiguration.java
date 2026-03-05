package com.vengine.kk.sap.autoconfigure;

import com.vengine.kk.sap.common.config.SapProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableConfigurationProperties(SapProperties.class)
@ComponentScan(basePackages = "com.vengine.kk.sap")
public class SapAutoConfiguration {
}
