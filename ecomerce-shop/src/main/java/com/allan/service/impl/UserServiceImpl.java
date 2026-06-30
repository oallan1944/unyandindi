package com.allan.service.impl;

import org.springframework.stereotype.Service;

import com.allan.config.JwtProvider;
import com.allan.model.User;
import com.allan.repository.UserRepository;
import com.allan.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    
    @Override
    public User findUserByJwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJwtToken(jwt);

        return this.findUserByEmail(email);
    }

    @Override
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User with email not found" + email);
        }
        return user;
    }

    

      

}
