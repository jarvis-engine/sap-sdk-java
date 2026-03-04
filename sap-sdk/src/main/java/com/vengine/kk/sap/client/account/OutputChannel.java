package com.vengine.kk.sap.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutputChannel {

    public static final String PRINT_CHANNEL    = "PRT";
    public static final String EMAIL_CHANNEL    = "INT";
    public static final String FAX_CHANNEL      = "FAX";
    public static final String EXTERNAL_CHANNEL = "XMS";

    private String channel;

    @Nullable
    private String template;

    @Builder.Default
    private Map<String, Object> settings = java.util.Collections.emptyMap();
}
