package com.vengine.kk.sap.demo.controller;

import com.vengine.kk.sap.client.order.sales.SalesOrder;
import com.vengine.kk.sap.client.order.sales.SalesOrderClient;
import com.vengine.kk.sap.common.model.SapQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demo/sales-orders")
public class SalesOrderController {

    private final SalesOrderClient salesOrderClient;

    public SalesOrderController(SalesOrderClient salesOrderClient) {
        this.salesOrderClient = salesOrderClient;
    }

    @GetMapping
    public List<SalesOrder> list(@RequestParam(defaultValue = "10") String limit) {
        SapQuery query = new SapQuery();
        query.setLimit(limit);
        return salesOrderClient.fetch(query);
    }

    @GetMapping("/{id}")
    public SalesOrder getOne(@PathVariable String id) {
        return salesOrderClient.fetchOne(id);
    }
}
