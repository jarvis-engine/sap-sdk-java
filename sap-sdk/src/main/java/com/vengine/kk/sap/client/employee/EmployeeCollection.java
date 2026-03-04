package com.vengine.kk.sap.client.employee;

import com.vengine.kk.sap.common.model.PageMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCollection {

    private List<Employee> items;

    @Nullable
    private PageMetadata metadata;
}
