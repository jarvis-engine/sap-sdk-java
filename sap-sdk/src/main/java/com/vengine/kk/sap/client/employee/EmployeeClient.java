package com.vengine.kk.sap.client.employee;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.model.SapQuery;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * SAP ByDesign client for employee operations.
 */
@Slf4j
public class EmployeeClient extends BaseSapClient implements EmployeeClientInterface {

    private static final String EMPLOYEE_GET = "v1/employee/get";

    public EmployeeClient(SapAuthenticatedClientFactory factory,
                          SapProperties properties,
                          SapResponseDecoder decoder) {
        super(factory, properties, decoder);
    }

    /**
     * Fetches a paginated list of employees.
     */
    public List<Employee> fetch(SapQuery query) {
        return getList(appendQueryParams(EMPLOYEE_GET, query != null ? query.toParamMap() : java.util.Map.of()), Employee.class);
    }
}
