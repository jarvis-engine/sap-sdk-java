package com.vengine.kk.sap.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.client.account.AccountClient;
import com.vengine.kk.sap.client.account.AccountClientInterface;
import com.vengine.kk.sap.client.account.mock.AccountClientMock;
import com.vengine.kk.sap.client.delivery.DeliveryCostClient;
import com.vengine.kk.sap.client.delivery.DeliveryCostClientInterface;
import com.vengine.kk.sap.client.delivery.mock.DeliveryCostClientMock;
import com.vengine.kk.sap.client.employee.EmployeeClient;
import com.vengine.kk.sap.client.employee.EmployeeClientInterface;
import com.vengine.kk.sap.client.employee.mock.EmployeeClientMock;
import com.vengine.kk.sap.client.order.rental.RentalOrderClient;
import com.vengine.kk.sap.client.order.rental.RentalOrderClientInterface;
import com.vengine.kk.sap.client.order.rental.mock.RentalOrderClientMock;
import com.vengine.kk.sap.client.order.sales.SalesOrderClient;
import com.vengine.kk.sap.client.order.sales.SalesOrderClientInterface;
import com.vengine.kk.sap.client.order.sales.mock.SalesOrderClientMock;
import com.vengine.kk.sap.client.product.catalog.ProductClient;
import com.vengine.kk.sap.client.product.catalog.ProductClientInterface;
import com.vengine.kk.sap.client.product.catalog.mock.ProductClientMock;
import com.vengine.kk.sap.client.product.rental.RentalProductClient;
import com.vengine.kk.sap.client.product.rental.RentalProductClientInterface;
import com.vengine.kk.sap.client.product.rental.mock.RentalProductClientMock;
import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

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
    public AccountClientInterface accountClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new AccountClient(factory, properties, decoder);
    }

    @Bean
    @Profile("sap-mock")
    public AccountClientInterface accountClientMock() {
        return new AccountClientMock();
    }

    @Bean
    @ConditionalOnMissingBean
    public SalesOrderClientInterface salesOrderClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new SalesOrderClient(factory, properties, decoder);
    }

    @Bean
    @Profile("sap-mock")
    public SalesOrderClientInterface salesOrderClientMock() {
        return new SalesOrderClientMock();
    }

    @Bean
    @ConditionalOnMissingBean
    public RentalOrderClientInterface rentalOrderClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new RentalOrderClient(factory, properties, decoder);
    }

    @Bean
    @Profile("sap-mock")
    public RentalOrderClientInterface rentalOrderClientMock() {
        return new RentalOrderClientMock();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProductClientInterface productClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new ProductClient(factory, properties, decoder);
    }

    @Bean
    @Profile("sap-mock")
    public ProductClientInterface productClientMock() {
        return new ProductClientMock();
    }

    @Bean
    @ConditionalOnMissingBean
    public RentalProductClientInterface rentalProductClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new RentalProductClient(factory, properties, decoder);
    }

    @Bean
    @Profile("sap-mock")
    public RentalProductClientInterface rentalProductClientMock() {
        return new RentalProductClientMock();
    }

    @Bean
    @ConditionalOnMissingBean
    public EmployeeClientInterface employeeClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new EmployeeClient(factory, properties, decoder);
    }

    @Bean
    @Profile("sap-mock")
    public EmployeeClientInterface employeeClientMock() {
        return new EmployeeClientMock();
    }

    @Bean
    @ConditionalOnMissingBean
    public DeliveryCostClientInterface deliveryCostClient(SapAuthenticatedClientFactory factory,
            SapProperties properties, SapResponseDecoder decoder) {
        return new DeliveryCostClient(factory, properties, decoder);
    }

    @Bean
    @Profile("sap-mock")
    public DeliveryCostClientInterface deliveryCostClientMock() {
        return new DeliveryCostClientMock();
    }
}
