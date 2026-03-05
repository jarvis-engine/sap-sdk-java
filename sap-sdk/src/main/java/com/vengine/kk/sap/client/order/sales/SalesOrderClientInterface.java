package com.vengine.kk.sap.client.order.sales;

import com.vengine.kk.sap.common.model.SapQuery;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Interface for SAP ByDesign Sales Order operations.
 * Implement this interface in tests to mock SAP sales order calls.
 */
public interface SalesOrderClientInterface {

    List<SalesOrder> fetch(@Nullable SapQuery query);

    SalesOrder fetchOne(String id);

    SalesOrder create(Map<String, Object> request);

    SalesOrder update(String id, Map<String, Object> request);

    void cancel(String id);
}
