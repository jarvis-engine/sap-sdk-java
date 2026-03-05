package com.vengine.kk.sap.client.product.rental.mock;

import com.vengine.kk.sap.client.product.rental.RentalProductClientInterface;
import com.vengine.kk.sap.client.product.rental.SerializedItem;
import com.vengine.kk.sap.common.model.SapQuery;

import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link RentalProductClientInterface} for use in tests.
 * All methods return empty collections or null by default.
 * TODO: override in tests
 */
public class RentalProductClientMock implements RentalProductClientInterface {

    public RentalProductClientMock() {
    }

    @Override
    public List<SerializedItem> fetchSerializedItems(SapQuery query) {
        // TODO: override in tests
        return Collections.emptyList();
    }
}
