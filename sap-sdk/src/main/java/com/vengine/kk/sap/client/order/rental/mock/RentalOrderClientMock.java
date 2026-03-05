package com.vengine.kk.sap.client.order.rental.mock;

import com.vengine.kk.sap.client.order.rental.RentalOrder;
import com.vengine.kk.sap.client.order.rental.RentalOrderClientInterface;
import com.vengine.kk.sap.client.order.rental.RentalOrderInput;
import com.vengine.kk.sap.client.order.rental.RentalPriceCheckInput;
import com.vengine.kk.sap.client.order.rental.RentalPriceCheckProduct;
import com.vengine.kk.sap.client.order.rental.RentalRateCheck;
import com.vengine.kk.sap.client.order.rental.RentalRateCheckInput;

import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link RentalOrderClientInterface} for use in tests.
 * All methods return empty collections or null by default.
 * TODO: override in tests
 */
public class RentalOrderClientMock implements RentalOrderClientInterface {

    public RentalOrderClientMock() {
    }

    @Override
    public RentalOrder create(RentalOrderInput input) {
        // TODO: override in tests
        return null;
    }

    @Override
    public List<RentalPriceCheckProduct> checkPrice(RentalPriceCheckInput input) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public RentalRateCheck checkRate(RentalRateCheckInput input) {
        // TODO: override in tests
        return null;
    }
}
