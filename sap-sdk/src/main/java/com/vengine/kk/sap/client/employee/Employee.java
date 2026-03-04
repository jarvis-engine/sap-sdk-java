package com.vengine.kk.sap.client.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private String uuid;
    private String employeeId;
    private String firstName;
    private String lastName;

    @Nullable
    private String phone;

    @Nullable
    private String changeStateId;

    @Nullable
    private String workEmail;

    @Nullable
    private String reportingLineUnitId;

    @Nullable
    private String customerInternalId;

    @Nullable
    private String companyId;
}
