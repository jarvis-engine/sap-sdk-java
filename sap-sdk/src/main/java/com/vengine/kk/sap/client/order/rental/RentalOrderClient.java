package com.vengine.kk.sap.client.order.rental;

import com.fasterxml.jackson.databind.JsonNode;
import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SAP ByDesign client for rental order operations.
 *
 * <p>Covers the rental order lifecycle:
 * <ul>
 *   <li>{@link #create} — creates a service order in SAP
 *   <li>{@link #checkPrice} — calculates rental price for a date range
 *   <li>{@link #checkRate} — fetches rental rate tiers (daily/weekly/monthly)
 * </ul>
 *
 * <p>All three endpoints use SAP's custom SOAP/REST envelope format (not OData),
 * which is why this client uses the {@code *WithNode} methods from {@link BaseSapClient}.
 *
 * <p>Request payload field names mirror PHP's {@code RentalOrderNormalizer},
 * {@code RentalPriceCheckNormalizer}, and {@code RentalRateCheckNormalizer}.
 */
@Slf4j
public class RentalOrderClient extends BaseSapClient {

    private static final String RENTAL_ORDER_CREATE      = "v1/service-order/post";
    private static final String RENTAL_ORDER_CHECK_PRICE = "v1/service-order/rental-price/get";
    private static final String RENTAL_ORDER_CHECK_RATE  = "v1/rental-rate/post";

    private static final String NODE_SERVICE_ORDER        = "ServiceOrder";
    private static final String NODE_ITEM                 = "Item";
    private static final String NODE_RENTAL_RATE_CALC     = "RentalRateCalculator";

    private static final DateTimeFormatter SAP_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
    private static final DateTimeFormatter SAP_DATE     = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public RentalOrderClient(SapAuthenticatedClientFactory factory,
                             SapProperties properties,
                             SapResponseDecoder decoder) {
        super(factory, properties, decoder);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // create
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a rental (service) order in SAP ByDesign.
     *
     * <p>SAP response envelope: {@code n0:ServiceOrderByElementsResponse_synC} → {@code ServiceOrder}
     *
     * @param input typed rental order input
     * @return SAP-assigned internalId (e.g. {@code RO-2026-0042}) and uuid
     */
    public RentalOrder create(RentalOrderInput input) {
        Map<String, Object> payload = buildCreatePayload(input);
        return postWithNode(RENTAL_ORDER_CREATE, payload, NODE_SERVICE_ORDER, RentalOrder.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // checkPrice
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calculates rental prices for one or more machines over a date range.
     *
     * <p>SAP response: list of {@code Item} nodes, each containing {@code NetPrice},
     * {@code NetValue}, and {@code PriceComponents} (condition types 7PR1, 7PR6).
     *
     * @param input pricing request with products and date range
     * @return list of price results, one per product
     */
    public List<RentalPriceCheckProduct> checkPrice(RentalPriceCheckInput input) {
        Map<String, Object> payload = buildPriceCheckPayload(input);
        List<JsonNode> rawItems = postRawListWithNode(RENTAL_ORDER_CHECK_PRICE, payload, NODE_ITEM);
        return rawItems.stream().map(this::parsePriceItem).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // checkRate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetches rental rate tiers (daily/weekly/monthly) for a machine.
     *
     * <p>SAP response envelope: {@code n0:RentalRateCalculatorReadByIDResponse_sync} → {@code RentalRateCalculator}
     *
     * @param input rate check request
     * @return rental rate result with uuid, rentalRate, rentalRateName
     */
    public RentalRateCheck checkRate(RentalRateCheckInput input) {
        Map<String, Object> payload = buildRateCheckPayload(input);
        return postWithNode(RENTAL_ORDER_CHECK_RATE, payload, NODE_RENTAL_RATE_CALC, RentalRateCheck.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payload builders (mirrors PHP normalizers)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Mirrors PHP's {@code RentalOrderNormalizer.normalize()}.
     * Field names match SAP's expected JSON exactly.
     */
    private Map<String, Object> buildCreatePayload(RentalOrderInput order) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("uuid",              order.getUuid());
        payload.put("deliveryBranch",    order.getDeliveryBranch());
        payload.put("sourceSystem",      order.getSystem());
        payload.put("startDateTime",     order.getStartDate()   != null ? order.getStartDate().format(SAP_DATETIME) : null);
        payload.put("endDateTime",       order.getEndDate()     != null ? order.getEndDate().format(SAP_DATETIME)   : null);
        payload.put("originCreationDate",order.getCreatedAt()   != null ? order.getCreatedAt().format(SAP_DATETIME) : null);
        payload.put("serviceOrderEnabledForFieldVu", order.isServiceOrderEnabledForFieldVu());
        payload.put("name",              order.getName());
        payload.put("buyerId",           order.getBuyerId());
        payload.put("currency",          order.getCurrency());
        payload.put("customer",          Map.of("internalId", nullSafe(order.getCustomerId())));
        payload.put("accountParty",      normalizeParty(order.getAccountParty()));
        payload.put("billToParty",       normalizeParty(order.getBillingParty()));
        payload.put("productRecipientParty", normalizeParty(order.getDeliveryParty()));
        payload.put("employeeResponsibleParty", Map.of("internalId", nullSafe(order.getEmployeeId())));
        payload.put("sellerParty",       Map.of("internalId", nullSafe(order.getSellerParty())));
        payload.put("fieldVuDeliveryBranch", order.getFieldVuDeliveryBranch());
        payload.put("serviceExecutionParty", Map.of("internalId", nullSafe(order.getServiceExecutionParty())));
        payload.put("release",           order.isRelease());
        payload.put("products",          normalizeProducts(order.getProducts()));
        if (order.getSalesUnitId() != null) {
            payload.put("salesUnitParty", Map.of("internalId", order.getSalesUnitId()));
        }
        return payload;
    }

    /**
     * Mirrors PHP's {@code RentalPriceCheckNormalizer.normalize()}.
     * Key difference: Java field {@code salesUnitId} → SAP field {@code salesOrganisationId}.
     */
    private Map<String, Object> buildPriceCheckPayload(RentalPriceCheckInput input) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("application",         "2");  // constant per PHP normalizer
        payload.put("currencyCode",         input.getCurrency());
        payload.put("pricingDate",          input.getPricingDate() != null ? input.getPricingDate().format(SAP_DATE) : null);
        payload.put("accountId",            input.getAccountId());
        payload.put("salesOrganisationId",  input.getSalesUnitId());   // NOTE: different name than Java field
        payload.put("distributionChannel",  input.getDistributionChannel());
        payload.put("companyId",            input.getSellerParty());
        payload.put("pricingModel",         input.getPricingModel());
        payload.put("items",                normalizePriceCheckProducts(input.getProducts()));
        return payload;
    }

    /**
     * Mirrors PHP's {@code RentalRateCheckNormalizer.normalize()}.
     * Key difference: SAP field is {@code companyID} (capital D).
     */
    private Map<String, Object> buildRateCheckPayload(RentalRateCheckInput input) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("calculationMode",      input.getCalculationMode());
        payload.put("fixedReturnIndicator", input.isFixedReturnIndicator());
        payload.put("companyID",            input.getCompanyId());   // capital D — SAP quirk
        payload.put("planningPossible",     input.isPlanningPossible());
        payload.put("quantity",             input.getQuantity());
        return payload;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Response parsers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Parses a single SAP price item into {@link RentalPriceCheckProduct}.
     *
     * <p>Mirrors PHP's {@code RentalPriceCheckNormalizer.denormalize()} logic:
     * <ul>
     *   <li>ConditionType {@code 7PR1} → netBasePrice
     *   <li>ConditionType {@code 7PR6} → netDiscount + discountPercentage (summed if multiple)
     * </ul>
     */
    private RentalPriceCheckProduct parsePriceItem(JsonNode item) {
        String currency = item.path("NetPrice").path("CurrencyCode").asText(null);

        RentalPriceCheckProduct product = new RentalPriceCheckProduct();
        product.setInternalId(item.path("ProductID").asText(null));
        product.setCurrency(currency);
        product.setNetUnitPriceAmount(item.path("NetPrice").path("DecimalValue").asText(null));
        product.setNetTotalPriceAmount(item.path("NetValue").path("DecimalValue").asText(null));

        JsonNode components = item.path("PriceComponents");
        List<JsonNode> componentList = new ArrayList<>();
        if (components.isArray()) {
            components.forEach(componentList::add);
        } else if (components.isObject()) {
            componentList.add(components); // SAP single-object quirk
        }

        double discountAmount = 0.0;
        double discountPct    = 0.0;

        for (JsonNode comp : componentList) {
            String condType = comp.path("ConditionType").asText("");
            if ("7PR1".equals(condType)) {
                product.setNetBasePriceAmount(comp.path("ConditionRate").asText(null));
            }
            if ("7PR6".equals(condType)) {
                discountAmount += parseDouble(comp.path("ConditionValue").asText("0"));
                discountPct    += parseDouble(comp.path("ConditionRate").asText("0"));
            }
        }

        if (discountAmount != 0.0) {
            product.setNetDiscountAmount(String.valueOf(discountAmount));
            product.setDiscountPercentage(String.valueOf(discountPct));
        }

        return product;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Normalizer helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, Object> normalizeParty(RentalOrderParty party) {
        if (party == null) return Map.of("internalId", "");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("internalId", nullSafe(party.getInternalId()));
        map.put("name",       nullSafe(party.getName()));
        return map;
    }

    private List<Map<String, Object>> normalizeProducts(List<RentalOrderProductInput> products) {
        if (products == null) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (RentalOrderProductInput p : products) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("internalId",   p.getInternalId());
            item.put("quantity",     p.getQuantity());
            item.put("startDateTime", p.getStartDateTime() != null ? p.getStartDateTime().format(SAP_DATETIME) : null);
            item.put("endDateTime",   p.getEndDateTime()   != null ? p.getEndDateTime().format(SAP_DATETIME)   : null);
            item.put("discount",     p.getDiscount());
            item.put("productPrice", p.getProductPrice());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> normalizePriceCheckProducts(List<RentalPriceCheckProductInput> products) {
        if (products == null) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (RentalPriceCheckProductInput p : products) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId",       p.getInternalId());
            item.put("productTypeCode", p.getProductTypeCode());
            item.put("quantity",        p.getQuantity());
            item.put("supplierId",      p.getSupplierId());
            item.put("pricingModel",    p.getPricingModel());
            result.add(item);
        }
        return result;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private double parseDouble(String value) {
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return 0.0; }
    }
}
