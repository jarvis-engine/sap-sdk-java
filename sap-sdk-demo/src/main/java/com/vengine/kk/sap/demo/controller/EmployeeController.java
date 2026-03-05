package com.vengine.kk.sap.demo.controller;

import com.vengine.kk.sap.client.employee.Employee;
import com.vengine.kk.sap.client.employee.EmployeeClient;
import com.vengine.kk.sap.common.model.SapQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demo/employees")
public class EmployeeController {

    private final EmployeeClient employeeClient;

    public EmployeeController(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    @GetMapping
    public List<Employee> list(@RequestParam(defaultValue = "10") String limit) {
        SapQuery query = new SapQuery();
        query.setLimit(limit);
        return employeeClient.fetch(query);
    }
}
