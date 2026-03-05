package com.vengine.kk.sap.client.order.sales.mock;

import com.vengine.kk.sap.client.order.sales.SalesOrder;
import com.vengine.kk.sap.client.order.sales.SalesOrderClientInterface;
import com.vengine.kk.sap.common.model.SapQuery;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of {@link SalesOrderClientInterface} for use in tests.
 * All methods return empty collections or null by default.
 * TODO: override in tests
 */
public class SalesOrderClientMock implements SalesOrderClientInterface {

    public SalesOrderClientMock() {
    }

    @Override
    public List<SalesOrder> fetch(@Nullable SapQuery query) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public SalesOrder fetchOne(String id) {
        // TODO: override in tests
        return null;
    }

    @Override
    public SalesOrder create(Map<String, Object> request) {
        // TODO: override in tests
        return null;
    }

    @Override
    public SalesOrder update(String id, Map<String, Object> request) {
        // TODO: override in tests
        return null;
    }

    @Override
    public void cancel(String id) {
        // TODO: override in tests
    }
}
