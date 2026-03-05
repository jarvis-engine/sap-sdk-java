package com.vengine.kk.sap.client.product.rental;

import com.vengine.kk.sap.common.model.SapQuery;

import java.util.List;

/**
 * Interface for SAP ByDesign Rental Product operations.
 * Implement this interface in tests to mock SAP rental product calls.
 */
public interface RentalProductClientInterface {

    List<SerializedItem> fetchSerializedItems(SapQuery query);
}
