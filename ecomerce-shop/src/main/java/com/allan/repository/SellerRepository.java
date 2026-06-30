package com.allan.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.allan.model.Seller;
import com.allan.domain.AccountStatus;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    Seller findByEmail(String email);

    List<Seller> findByAccountStatus(AccountStatus status);

  

   
    
}