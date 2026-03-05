package com.vengine.kk.sap.client.delivery.mock;

import com.vengine.kk.sap.client.delivery.DeliveryCost;
import com.vengine.kk.sap.client.delivery.DeliveryCostClientInterface;

import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link DeliveryCostClientInterface} for use in tests.
 * All methods return empty collections or null by default.
 * TODO: override in tests
 */
public class DeliveryCostClientMock implements DeliveryCostClientInterface {

    public DeliveryCostClientMock() {
    }

    @Override
    public List<DeliveryCost> fetch(String fromPostalCode, String toPostalCode, String weight) {
        // TODO: override in tests
        return Collections.emptyList();
    }
}
