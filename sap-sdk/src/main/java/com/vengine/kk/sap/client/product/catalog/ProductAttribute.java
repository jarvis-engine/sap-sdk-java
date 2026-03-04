package com.vengine.kk.sap.client.product.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {

    private String uuid;

    @Nullable
    private String unitCode;

    @Nullable
    private String unitName;

    @Nullable
    private String productId;

    private String value;

    /** Translated names keyed by language code, e.g. "de" → name. */
    @Builder.Default
    private List<AttributeTranslation> translations = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeTranslation {

        public static final String LANGUAGE_DE = "DE";
        public static final String LANGUAGE_EN = "EN";

        private String language;
        private String name;
    }
}
