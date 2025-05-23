package com.imovel.api.controller;

import com.imovel.api.response.StandardResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
class ReviewDeleteController {

    @DeleteMapping("/{id}")
    public StandardResponse<Void> deleteReview(@PathVariable String id) {
        // TODO: Implement delete review logic
        return new StandardResponse<>();
    }
}