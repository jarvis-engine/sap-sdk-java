package com.vengine.kk.sap.client.delivery;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * SAP ByDesign client for delivery/shipping cost operations.
 */
@Slf4j
public class DeliveryCostClient extends BaseSapClient {

    private static final String DELIVERY_COST_GET = "v1/sales-order/shipping-costs/get";

    public DeliveryCostClient(SapAuthenticatedClientFactory factory,
                              SapProperties properties,
                              SapResponseDecoder decoder) {
        super(factory, properties, decoder);
    }

    /**
     * Fetches delivery/shipping costs for a given postal code range and weight.
     *
     * @param fromPostalCode origin postal code
     * @param toPostalCode   destination postal code
     * @param weight         shipment weight
     */
    public List<DeliveryCost> fetch(String fromPostalCode, String toPostalCode, String weight) {
        String route = appendQueryParams(DELIVERY_COST_GET, java.util.Map.of(
                "fromPostalCode", fromPostalCode,
                "toPostalCode", toPostalCode,
                "weight", weight
        ));
        return getList(route, DeliveryCost.class);
    }
}
