package com.allan.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByEmail(String email);

    // Optional<Admin> findById(Long id);

    boolean existsByEmail(String email);

}