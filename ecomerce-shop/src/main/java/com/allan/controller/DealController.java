package com.allan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.allan.model.Deal;
import com.allan.response.ApiResponse;
import com.allan.service.DealService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/deals")
@PreAuthorize("hasRole('ADMIN')")
public class DealController {

    private final DealService dealService;

    @GetMapping
    public ResponseEntity<List<Deal>> getDeal() {
        List<Deal> createdDeals = dealService.getDeals();

        return new ResponseEntity<>(createdDeals, HttpStatus.ACCEPTED);
    }

    @PostMapping
    public ResponseEntity<Deal> createDeal(@RequestBody Deal deals) {
        Deal createdDeals = dealService.createDeal(deals);

        return new ResponseEntity<>(createdDeals, HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Deal> updateDeal(
            @PathVariable Long id,
            @RequestBody Deal deal) throws Exception {
        Deal updatedDeal = dealService.updateDeal(deal, id);
        return ResponseEntity.ok(updatedDeal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDeals(@PathVariable Long id) throws Exception {
        dealService.deleteDeal(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Deal deleted");

        return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
    }

}





// package com.allan.controller;

// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PatchMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.allan.model.Deal;
// import com.allan.response.ApiResponse;
// import com.allan.service.DealService;

// import lombok.RequiredArgsConstructor;

// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/admin/deals")
// public class DealController {

//     private final DealService dealService;

//     @GetMapping
//     public ResponseEntity<List<Deal>> getDeal() {
//         List<Deal> createdDeals = dealService.getDeals();

//         return new ResponseEntity<>(createdDeals, HttpStatus.ACCEPTED);
//     }

//     @PostMapping
//     public ResponseEntity<Deal> createDeal(@RequestBody Deal deals) {
//         Deal createdDeals = dealService.createDeal(deals);

//         return new ResponseEntity<>(createdDeals, HttpStatus.ACCEPTED);
//     }

//     @PatchMapping("/{id}")
//     public ResponseEntity<Deal> updateDeal(
//             @PathVariable Long id,
//             @RequestBody Deal deal) throws Exception {
//         Deal updatedDeal = dealService.updateDeal(deal, id);
//         return ResponseEntity.ok(updatedDeal);
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<ApiResponse> deleteDeals(@PathVariable Long id) throws Exception {
//         dealService.deleteDeal(id);

//         ApiResponse apiResponse = new ApiResponse();
//         apiResponse.setMessage("Deal deleted");

//         return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
//     }

// }
