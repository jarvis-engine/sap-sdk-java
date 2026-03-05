package com.vengine.kk.sap.client.order.rental;

import java.util.List;

/**
 * Interface for SAP ByDesign Rental Order operations.
 * Implement this interface in tests to mock SAP rental order calls.
 */
public interface RentalOrderClientInterface {

    RentalOrder create(RentalOrderInput input);

    List<RentalPriceCheckProduct> checkPrice(RentalPriceCheckInput input);

    RentalRateCheck checkRate(RentalRateCheckInput input);
}
