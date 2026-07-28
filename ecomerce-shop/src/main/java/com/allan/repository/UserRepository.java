package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.allan.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ existing — unchanged
    User findByEmail(String email);

    // ✅ added — required by OAuth2UserServiceImpl.linkGoogleAccount
    // to prevent two accounts from linking the same Google identity.
    // Returns null if no account has this googleId — not Optional,
    // consistent with findByEmail pattern already in use.
    User findByGoogleId(String googleId);

    // ✅ added — used by OAuth2UserServiceImpl and CustomOAuth2UserService
    // to check existence without loading the full entity when only a
    // boolean answer is needed (avoids loading password hash unnecessarily).
    boolean existsByEmail(String email);
}