package com.vengine.kk.sap.client.employee;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.error.SapExceptionHandler;
import com.vengine.kk.sap.common.model.SapQuery;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SAP ByDesign client for employee operations.
 */
@Slf4j
@Component
public class EmployeeClient extends BaseSapClient {

    private static final String EMPLOYEE_GET = "v1/employee/get";

    public EmployeeClient(SapAuthenticatedClientFactory factory,
                          SapProperties properties,
                          SapResponseDecoder decoder,
                          SapExceptionHandler exceptionHandler) {
        super(factory, properties, decoder, exceptionHandler);
    }

    /**
     * Fetches a paginated list of employees.
     */
    public List<Employee> fetch(SapQuery query) {
        return getList(withQuery(EMPLOYEE_GET, query), Employee.class);
    }

    private String withQuery(String route, SapQuery query) {
        if (query == null) {
            return route;
        }
        StringBuilder sb = new StringBuilder(route);
        String sep = "?";
        if (query.getLimit() != null) {
            sb.append(sep).append("limit=").append(query.getLimit());
            sep = "&";
        }
        if (query.getLastId() != null) {
            sb.append(sep).append("lastId=").append(query.getLastId());
            sep = "&";
        }
        if (query.getCountryCode() != null) {
            sb.append(sep).append("countryCode=").append(query.getCountryCode());
        }
        return sb.toString();
    }
}
