package com.vengine.kk.sap.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SAP ByDesign Java SDK — Demo")
                .version("0.1.2")
                .description(
                    "Interactive demo of the `sap-sdk` library for SAP ByDesign OData APIs.\n\n" +
                    "## How to use this demo\n\n" +
                    "All requests run against a **WireMock** instance pre-loaded with realistic " +
                    "Kurt König GmbH data (Liebherr excavators, cranes, rental orders).\n\n" +
                    "### Recommended demo flow\n\n" +
                    "1. **GET** `/demo/rental/items` — see available machines with serial numbers\n" +
                    "2. **POST** `/demo/rental/price-check` — calculate rental price for a date range\n" +
                    "3. **POST** `/demo/rental/orders` — create rental order → get SAP order ID\n" +
                    "4. **GET** `/demo/rental/items/error-demo` — see SAP error → clean HTTP 502\n\n" +
                    "### Integration (add to your `rental-service`)\n\n" +
                    "```xml\n" +
                    "<dependency>\n" +
                    "  <groupId>com.vengine.kk</groupId>\n" +
                    "  <artifactId>sap-sdk</artifactId>\n" +
                    "  <version>0.1.2</version>\n" +
                    "</dependency>\n" +
                    "```\n\n" +
                    "```yaml\n" +
                    "sap:\n" +
                    "  base-url: https://my-sap.bydesign.cloud.sap\n" +
                    "  env: prod\n" +
                    "  credentials:\n" +
                    "    auth-type: BASIC\n" +
                    "    username: ${SAP_USER}\n" +
                    "    password: ${SAP_PASS}\n" +
                    "```"
                )
                .contact(new Contact()
                    .name("vengine GmbH")
                    .url("https://vengine.tech"))
            );
    }
}
