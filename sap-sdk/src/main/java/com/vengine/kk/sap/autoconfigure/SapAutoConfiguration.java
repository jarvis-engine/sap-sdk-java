package com.vengine.kk.sap.autoconfigure;

import com.vengine.kk.sap.common.config.SapProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(SapProperties.class)
public class SapAutoConfiguration {
}
