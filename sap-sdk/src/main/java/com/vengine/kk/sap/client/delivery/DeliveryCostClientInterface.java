package com.vengine.kk.sap.client.delivery;

import java.util.List;

/**
 * Interface for SAP ByDesign Delivery Cost operations.
 * Implement this interface in tests to mock SAP delivery cost calls.
 */
public interface DeliveryCostClientInterface {

    List<DeliveryCost> fetch(String fromPostalCode, String toPostalCode, String weight);
}
