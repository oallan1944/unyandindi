package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.allan.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}
