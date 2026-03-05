package com.vengine.kk.sap.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.client.account.AccountClient;
import com.vengine.kk.sap.client.delivery.DeliveryCostClient;
import com.vengine.kk.sap.client.employee.EmployeeClient;
import com.vengine.kk.sap.client.order.rental.RentalOrderClient;
import com.vengine.kk.sap.client.order.sales.SalesOrderClient;
import com.vengine.kk.sap.client.product.catalog.ProductClient;
import com.vengine.kk.sap.client.product.rental.RentalProductClient;
import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SapProperties.class)
@ConditionalOnProperty(prefix = "sap", name = "base-url")
public class SapAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SapResponseDecoder sapResponseDecoder(ObjectMapper objectMapper) {
        return new SapResponseDecoder(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public SapAuthenticatedClientFactory sapAuthenticatedClientFactory(
            SapProperties properties, ObjectMapper objectMapper) {
        return new SapAuthenticatedClientFactory(properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AccountClient accountClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new AccountClient(factory, properties, decoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public SalesOrderClient salesOrderClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new SalesOrderClient(factory, properties, decoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public RentalOrderClient rentalOrderClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new RentalOrderClient(factory, properties, decoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProductClient productClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new ProductClient(factory, properties, decoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public RentalProductClient rentalProductClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new RentalProductClient(factory, properties, decoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public EmployeeClient employeeClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new EmployeeClient(factory, properties, decoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public DeliveryCostClient deliveryCostClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new DeliveryCostClient(factory, properties, decoder);
    }
}
