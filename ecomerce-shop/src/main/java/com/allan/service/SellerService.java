package com.allan.service;

import java.util.List;

import com.allan.domain.AccountStatus;
import com.allan.exceptions.SellerException;
import com.allan.model.Seller;

public interface SellerService {

    Seller getSellerProfile(String jwt) throws Exception;

    Seller createSeller(Seller seller) throws Exception;

    Seller getSellerbyId(Long id) throws SellerException;

    Seller getSellerByEmail(String email) throws Exception;

    List<Seller> getAllSellers(AccountStatus status);

    Seller updateSeller(Long id, Seller seller) throws Exception;

    void deleteSeller(Long id) throws Exception;

    Seller verifyEmail(String email, String otp) throws Exception;

    Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception;

}
