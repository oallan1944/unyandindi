package com.allan.service;

import com.allan.model.User;

public interface UserService {

    User findUserByJwtToken(String jwt) throws Exception;

    User findUserByEmail(String email) throws Exception;

    

}
