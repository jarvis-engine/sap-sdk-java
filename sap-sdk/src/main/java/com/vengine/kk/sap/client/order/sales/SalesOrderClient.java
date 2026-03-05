package com.vengine.kk.sap.client.order.sales;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.model.SapQuery;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client for SAP ByDesign Sales Order operations.
 * Feature-flag-aware routing: V3/V4/V5 when enabled, V1/V2 fallback.
 */
@Slf4j
@Component
public class SalesOrderClient extends BaseSapClient {

    // V1 routes
    private static final String ORDER_V1_GET    = "v1/sales-order/get";
    private static final String ORDER_V1_CANCEL = "v1/sales-order/cancel";

    // V2 routes
    private static final String ORDER_V2_POST       = "v2/sales-order/post";
    private static final String ORDER_V2_GET_BY_ID  = "v2/sales-order/get";

    // V3–V5 routes (feature-flag controlled)
    private static final String ORDER_V3_GET  = "v3/sales-order/get";
    private static final String ORDER_V3_POST = "v3/sales-order/post";
    private static final String ORDER_V4_GET  = "v4/sales-order/get";
    private static final String ORDER_V4_POST = "v4/sales-order/post";
    private static final String ORDER_V5_GET  = "v5/sales-order/get";
    private static final String ORDER_V5_POST = "v5/sales-order/post";

    public SalesOrderClient(SapAuthenticatedClientFactory factory,
                            SapProperties properties,
                            SapResponseDecoder decoder) {
        super(factory, properties, decoder);
    }

    public List<SalesOrder> fetch(@Nullable SapQuery query) {
        String route = appendQueryParams(fetchRoute(), query != null ? query.toParamMap() : Map.of());
        return getList(route, SalesOrder.class);
    }

    public SalesOrder fetchOne(String id) {
        String route = appendQueryParams(ORDER_V2_GET_BY_ID, Map.of("id", id));
        return get(route, SalesOrder.class);
    }

    public SalesOrder create(Map<String, Object> request) {
        return post(createRoute(), request, SalesOrder.class);
    }

    public SalesOrder update(String id, Map<String, Object> request) {
        request.put("id", id);
        return post(createRoute(), request, SalesOrder.class);
    }

    public void cancel(String id) {
        postVoid(ORDER_V1_CANCEL, Collections.singletonMap("id", id));
    }

    // ── Feature-flag routing ──────────────────────────────────────────────────

    private String fetchRoute() {
        SapProperties.Features f = properties.getFeatures();
        if (f.isSalesOrderV5EndpointEnabled()) return ORDER_V5_GET;
        if (f.isSalesOrderV4EndpointEnabled()) return ORDER_V4_GET;
        if (f.isSalesOrderV3EndpointEnabled()) return ORDER_V3_GET;
        return ORDER_V1_GET;
    }

    private String createRoute() {
        SapProperties.Features f = properties.getFeatures();
        if (f.isSalesOrderV5EndpointEnabled()) return ORDER_V5_POST;
        if (f.isSalesOrderV4EndpointEnabled()) return ORDER_V4_POST;
        if (f.isSalesOrderV3EndpointEnabled()) return ORDER_V3_POST;
        return ORDER_V2_POST;
    }
}
