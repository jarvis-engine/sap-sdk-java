package com.vengine.kk.sap.client.product.rental;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.error.SapExceptionHandler;
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
                               SapResponseDecoder decoder,
                               SapExceptionHandler exceptionHandler) {
        super(factory, properties, decoder, exceptionHandler);
    }

    /**
     * Fetches serialized rental items (equipment with serial numbers).
     */
    public List<SerializedItem> fetchSerializedItems(SapQuery query) {
        return getList(withQuery(SERIALIZED_ITEMS, query), SerializedItem.class);
    }

    private String withQuery(String route, SapQuery query) {
        if (query == null) {
            return route;
        }
        StringBuilder sb = new StringBuilder(route);
        String sep = "?";
        if (query.getLimit() != null) {
            sb.append(sep).append("limit=").append(query.getLimit());
            sep = "&";
        }
        if (query.getLastId() != null) {
            sb.append(sep).append("lastId=").append(query.getLastId());
            sep = "&";
        }
        if (query.getCountryCode() != null) {
            sb.append(sep).append("countryCode=").append(query.getCountryCode());
        }
        return sb.toString();
    }
}
