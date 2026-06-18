package com.aaron.usermanagement.repository;

import com.aaron.usermanagement.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {}
