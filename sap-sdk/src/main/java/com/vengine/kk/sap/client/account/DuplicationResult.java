package com.vengine.kk.sap.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicationResult {

    private boolean duplicateEmail;
    private boolean duplicateTaxId;
    private boolean duplicateCrefoId;
    private boolean duplicateContactPerson;
}
