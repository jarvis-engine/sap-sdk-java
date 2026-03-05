package com.vengine.kk.sap.demo.controller;

import com.vengine.kk.sap.client.product.rental.RentalProductClient;
import com.vengine.kk.sap.client.product.rental.SerializedItem;
import com.vengine.kk.sap.common.model.SapQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demo/rental")
public class RentalController {

    private final RentalProductClient rentalProductClient;

    public RentalController(RentalProductClient rentalProductClient) {
        this.rentalProductClient = rentalProductClient;
    }

    @GetMapping("/items")
    public List<SerializedItem> listItems(@RequestParam(defaultValue = "10") String limit) {
        SapQuery query = new SapQuery();
        query.setLimit(limit);
        return rentalProductClient.fetchSerializedItems(query);
    }
}
