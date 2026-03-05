package com.vengine.kk.sap.demo.controller;

import com.vengine.kk.sap.client.product.catalog.*;
import com.vengine.kk.sap.common.model.SapQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demo/products")
public class ProductController {

    private final ProductClient productClient;

    public ProductController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping
    public List<Product> list(@RequestParam(defaultValue = "10") String limit) {
        SapQuery query = new SapQuery();
        query.setLimit(limit);
        return productClient.fetch(query);
    }

    @GetMapping("/{id}")
    public Product getOne(@PathVariable String id) {
        return productClient.fetchOne(id);
    }

    @GetMapping("/{id}/availability")
    public ProductAvailability getAvailability(
            @PathVariable String id,
            @RequestParam("from") String fromDate,
            @RequestParam("to") String toDate) {
        return productClient.fetchAvailability(id, fromDate, toDate);
    }

    @GetMapping("/categories")
    public List<ProductCategory> getCategories() {
        return productClient.fetchCategories();
    }

    @GetMapping("/price-lists")
    public List<PriceList> getPriceLists() {
        return productClient.fetchSalesPriceLists();
    }
}
