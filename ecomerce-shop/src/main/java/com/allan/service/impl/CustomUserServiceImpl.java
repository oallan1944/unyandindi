package com.allan.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.allan.domain.USER_ROLE;
import com.allan.model.Admin;
import com.allan.model.Seller;
import com.allan.model.User;
import com.allan.repository.AdminRepository;
import com.allan.repository.SellerRepository;
import com.allan.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;
    private static final String SELLER_PREFIX = "seller_";
  

    @Override
    // This method is used by Spring Security to load user details by username
    // It checks if the username starts with "seller_" to determine if it's a seller
    // or a regular user
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

         log.info("Loading user by username: {}", username);

        if (username.startsWith(SELLER_PREFIX)) {
            String actualUsername = username.substring(SELLER_PREFIX.length());
            Seller seller = sellerRepository.findByEmail(actualUsername);
            if (seller != null) {
                return buildUserDetails(seller.getEmail(), seller.getPassword(), seller.getRole());
            }

        } 

        Admin admin = adminRepository.findByEmail(username);

        if (admin != null) {
            return buildUserDetails(
                    admin.getEmail(),
                    admin.getPassword(),
                    admin.getRole());
        }

        else {
            User user = userRepository.findByEmail(username);
            if (user != null) {
                return buildUserDetails(user.getEmail(), user.getPassword(), user.getRole());
            }
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    private UserDetails buildUserDetails(String email, String password, USER_ROLE role) {
        if (role == null)
            role = USER_ROLE.ROLE_CUSTOMER;

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(role.toString()));
        return new org.springframework.security.core.userdetails.User(
                email,
                password,
                authorityList);
    }
}
