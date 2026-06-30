package com.allan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allan.response.ApiResponse;

@RestController
public class HomeController {
   @GetMapping
   public ApiResponse HomeControllerHandler() {
      ApiResponse apiResponse = new ApiResponse();
      apiResponse.setMessage("Welcome to ecomerce multivendor system");
      return apiResponse;
   }
}
