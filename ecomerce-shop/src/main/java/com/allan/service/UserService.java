package com.allan.service;

import com.allan.dto.UserSummaryDTO;
import com.allan.model.User;
import com.allan.request.CompleteProfileRequest;


public interface UserService {

    User findUserByJwtToken(String jwt) throws Exception;

    User findUserByEmail(String email) throws Exception;

    UserSummaryDTO completeProfile(User user, CompleteProfileRequest request);

    

}
