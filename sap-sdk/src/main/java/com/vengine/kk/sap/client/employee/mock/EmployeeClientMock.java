package com.vengine.kk.sap.client.employee.mock;

import com.vengine.kk.sap.client.employee.Employee;
import com.vengine.kk.sap.client.employee.EmployeeClientInterface;
import com.vengine.kk.sap.common.model.SapQuery;

import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link EmployeeClientInterface} for use in tests.
 * All methods return empty collections or null by default.
 * TODO: override in tests
 */
public class EmployeeClientMock implements EmployeeClientInterface {

    public EmployeeClientMock() {
    }

    @Override
    public List<Employee> fetch(SapQuery query) {
        // TODO: override in tests
        return Collections.emptyList();
    }
}
