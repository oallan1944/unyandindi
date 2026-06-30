package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
