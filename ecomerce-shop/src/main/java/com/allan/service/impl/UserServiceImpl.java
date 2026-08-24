package com.allan.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.config.JwtProvider;
import com.allan.dto.UserSummaryDTO;
import com.allan.model.User;
import com.allan.repository.UserRepository;
import com.allan.request.CompleteProfileRequest;
import com.allan.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    
    @Override
    @Transactional(readOnly = true)
    public User findUserByJwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJwtToken(jwt);

        return this.findUserByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User with email not found" + email);
        }
        return user;
    }

    /**
     * Fills in fullName/mobile and flips profileComplete to true.
     * Not restricted to Google-origin accounts specifically — any
     * account with profileComplete=false can complete it this way,
     * which is the correct scope: profileComplete exists on User as
     * a general state, not a Google-only one.
     *
     * @Transactional (not readOnly) — this writes, unlike the two
     * lookup methods above.
     */
    @Override
    @Transactional
    public UserSummaryDTO completeProfile(User user, CompleteProfileRequest request) {
        user.setFullName(request.getFullName().trim());
        user.setMobile(request.getMobile().trim());
        user.setProfileComplete(true);
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(saved.getId());
        dto.setEmail(saved.getEmail());
        dto.setFullName(saved.getFullName());
        dto.setMobile(saved.getMobile());
        return dto;
    }

}