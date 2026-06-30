package com.allan.service;

import com.allan.domain.USER_ROLE;
import com.allan.request.LoginRequest;
import com.allan.response.AuthResponse;
import com.allan.response.SignupRequest;

public interface AuthService {

    void sentLoginOtp(String email, USER_ROLE role) throws Exception;

    String createUser(SignupRequest req) throws Exception;

    AuthResponse signing(LoginRequest req) throws Exception;

}
