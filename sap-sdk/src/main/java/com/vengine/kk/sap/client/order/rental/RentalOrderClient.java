package com.vengine.kk.sap.client.order.rental;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Client for SAP ByDesign Rental Order operations: create, checkPrice, checkRate.
 */
@Slf4j
public class RentalOrderClient extends BaseSapClient {

    private static final String RENTAL_ORDER_CREATE      = "v1/service-order/post";
    private static final String RENTAL_ORDER_CHECK_PRICE = "v1/service-order/rental-price/get";
    private static final String RENTAL_ORDER_CHECK_RATE  = "v1/rental-rate/post";

    public RentalOrderClient(SapAuthenticatedClientFactory factory,
                             SapProperties properties,
                             SapResponseDecoder decoder) {
        super(factory, properties, decoder);
    }

    public RentalOrder create(Map<String, Object> request) {
        return post(RENTAL_ORDER_CREATE, request, RentalOrder.class);
    }

    public RentalPriceCheck checkPrice(Map<String, Object> request) {
        return post(RENTAL_ORDER_CHECK_PRICE, request, RentalPriceCheck.class);
    }

    public RentalRateCheck checkRate(Map<String, Object> request) {
        return post(RENTAL_ORDER_CHECK_RATE, request, RentalRateCheck.class);
    }
}
