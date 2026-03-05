package com.vengine.kk.sap.client.product.rental;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.model.SapQuery;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SAP ByDesign client for rental product operations.
 */
@Slf4j
@Component
public class RentalProductClient extends BaseSapClient {

    private static final String SERIALIZED_ITEMS = "v1/service-product/get";

    public RentalProductClient(SapAuthenticatedClientFactory factory,
                               SapProperties properties,
                               SapResponseDecoder decoder) {
        super(factory, properties, decoder);
    }

    /**
     * Fetches serialized rental items (equipment with serial numbers).
     */
    public List<SerializedItem> fetchSerializedItems(SapQuery query) {
        return getList(appendQueryParams(SERIALIZED_ITEMS, query != null ? query.toParamMap() : java.util.Map.of()), SerializedItem.class);
    }
}
