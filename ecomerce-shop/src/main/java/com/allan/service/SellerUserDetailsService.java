package com.allan.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.allan.domain.USER_ROLE;
import com.allan.model.Seller;
import com.allan.repository.SellerRepository;

public class SellerUserDetailsService implements UserDetailsService {
    @Autowired
    private SellerRepository sellerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Seller seller = sellerRepository.findByEmail(email);
        if (seller == null) {
            throw new UsernameNotFoundException("Seller not found");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(USER_ROLE.ROLE_SELLER.toString()));

        return new org.springframework.security.core.userdetails.User(
            "seller_" + seller.getEmail(),
            "",
            authorities
        );
    }
}
