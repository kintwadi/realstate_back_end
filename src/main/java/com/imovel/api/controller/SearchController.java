package com.imovel.api.controller;

import com.imovel.api.response.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Search endpoints
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @GetMapping
    public StandardResponse<Void> searchProperties() {
        // TODO: Implement search properties logic
        return new StandardResponse<>();
    }

    @GetMapping("/suggestions")
    public StandardResponse<Void> getSearchSuggestions() {
        // TODO: Implement search suggestions logic
        return new StandardResponse<>();
    }
}