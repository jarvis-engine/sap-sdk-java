package com.vengine.kk.sap.client.product.rental;

/**
 * Validates a deserialized {@link SerializedItem} before it is returned to the caller.
 *
 * <p>Mirrors PHP's {@code SerializedItemValidator}: an item is valid only if it has
 * all three identifying fields. SAP sometimes returns incomplete FixedAsset records
 * (e.g. assets not yet fully configured) — these are silently skipped.
 */
public class SerializedItemValidator {

    /**
     * Returns {@code true} if the item has all required fields populated.
     */
    public boolean isValid(SerializedItem item) {
        return item.getArticleNumber() != null && !item.getArticleNumber().isBlank()
            && item.getSerialNumber()  != null && !item.getSerialNumber().isBlank()
            && item.getCostCenterId()  != null && !item.getCostCenterId().isBlank();
    }
}
