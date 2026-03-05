package com.vengine.kk.sap.client.product.rental;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.model.SapQuery;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * SAP ByDesign client for rental product operations.
 *
 * <p>Fetches serialized items (physical machines with serial numbers) from SAP.
 * SAP returns these under the {@code FixedAsset} node inside a {@code _sync} envelope.
 * Items missing required fields ({@link SerializedItemValidator}) are silently filtered.
 */
@Slf4j
public class RentalProductClient extends BaseSapClient implements RentalProductClientInterface {

    private static final String SERIALIZED_ITEMS = "v1/service-product/get";

    /** SAP response nodeKey for serialized rental items */
    private static final String NODE_FIXED_ASSET = "FixedAsset";

    private final SerializedItemValidator validator;

    public RentalProductClient(SapAuthenticatedClientFactory factory,
                               SapProperties properties,
                               SapResponseDecoder decoder) {
        super(factory, properties, decoder);
        this.validator = new SerializedItemValidator();
    }

    /**
     * Fetches all serialized rental items (equipment with serial numbers) from SAP.
     *
     * <p>Items that fail validation (missing VVS_UnitType / VVS_UnitNumber / CostCentreID)
     * are skipped — mirrors PHP's {@code SerializedItemValidator}.
     *
     * @param query optional pagination/filter params
     * @return validated list of serialized items
     */
    public List<SerializedItem> fetchSerializedItems(SapQuery query) {
        String route = appendQueryParams(
            SERIALIZED_ITEMS,
            query != null ? query.toParamMap() : Map.of()
        );
        List<SerializedItem> raw = getListWithNode(route, NODE_FIXED_ASSET, SerializedItem.class);
        List<SerializedItem> valid = raw.stream().filter(validator::isValid).toList();
        int skipped = raw.size() - valid.size();
        if (skipped > 0) {
            log.warn("Skipped {} invalid SerializedItem(s) (missing VVS_UnitType/Number/CostCentreID)", skipped);
        }
        return valid;
    }
}
