package com.vengine.kk.sap.client.account;

import com.vengine.kk.sap.common.model.PageMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCollection {

    private List<Discount> items;
    private PageMetadata metadata;
}
