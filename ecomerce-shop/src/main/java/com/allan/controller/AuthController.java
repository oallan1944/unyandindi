package com.allan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allan.domain.USER_ROLE;
import com.allan.model.User;
import com.allan.model.VerificationCode;
import com.allan.repository.UserRepository;
import com.allan.request.LoginOtpRequest;
import com.allan.request.LoginRequest;
import com.allan.response.ApiResponse;
//import com.allan.model.User;
//import com.allan.repository.UserRepository;
import com.allan.response.AuthResponse;
import com.allan.response.SignupRequest;
import com.allan.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping("/signup")
    // added throw Exception
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody SignupRequest req) throws Exception {

        String jwt = authService.createUser(req);

        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setMessage("Register success");
        res.setRole(USER_ROLE.ROLE_CUSTOMER);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/sent/login-signup-otp")
    // added throw Exception
    public ResponseEntity<ApiResponse> sentOtpHandler(@RequestBody LoginOtpRequest req)
            throws Exception {

        authService.sentLoginOtp(req.getEmail(), req.getRole());

        ApiResponse res = new ApiResponse();

        res.setMessage("Otp sent successfully");

        return ResponseEntity.ok(res);
    }

    @PostMapping("/signing")
    // added throw Exception
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody VerificationCode req)
            throws Exception {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(req.getEmail());
        loginRequest.setOtp(req.getOtp());

        AuthResponse authResponse = authService.signing(loginRequest);

        return ResponseEntity.ok(authResponse);
    }

}
