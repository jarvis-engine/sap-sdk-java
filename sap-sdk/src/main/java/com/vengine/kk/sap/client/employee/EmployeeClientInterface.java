package com.vengine.kk.sap.client.employee;

import com.vengine.kk.sap.common.model.SapQuery;

import java.util.List;

/**
 * Interface for SAP ByDesign Employee operations.
 * Implement this interface in tests to mock SAP employee calls.
 */
public interface EmployeeClientInterface {

    List<Employee> fetch(SapQuery query);
}
