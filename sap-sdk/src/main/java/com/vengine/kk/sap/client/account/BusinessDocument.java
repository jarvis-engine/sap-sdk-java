package com.vengine.kk.sap.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDocument {

    public static final String INVOICE_DOCUMENT_CODE = "28";

    private String code;
    private boolean enabled;

    @Builder.Default
    private List<OutputChannel> outputChannels = java.util.Collections.emptyList();
}
