package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Input for creating a rental (service) order in SAP ByDesign.
 *
 * <p>Mirrors PHP's {@code RentalOrderInput}. The SAP payload is built by
 * {@link RentalOrderClient} using this DTO — see {@code normalize()} there.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalOrderInput {

    private String uuid;
    private OffsetDateTime createdAt;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    /** Maps to SAP field {@code sourceSystem} */
    private String system;

    private String name;
    private String buyerId;

    @Builder.Default
    private String currency = "EUR";

    @Builder.Default
    private boolean serviceOrderEnabledForFieldVu = true;

    /** Maps to SAP nested field {@code customer.internalId} */
    private String customerId;

    /** Maps to SAP nested field {@code serviceExecutionParty.internalId} */
    private String serviceExecutionParty;

    /** Maps to SAP nested field {@code salesUnitParty.internalId} */
    private String salesUnitId;

    /** Maps to SAP nested field {@code sellerParty.internalId} */
    private String sellerParty;

    private RentalOrderParty accountParty;
    private RentalOrderParty billingParty;
    private RentalOrderParty deliveryParty;

    /** Maps to SAP nested field {@code employeeResponsibleParty.internalId} */
    private String employeeId;

    private String deliveryBranch;

    @Builder.Default
    private boolean release = false;

    private String fieldVuDeliveryBranch;

    @Builder.Default
    private List<RentalOrderProductInput> products = new ArrayList<>();
}
