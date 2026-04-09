package com.HelloWorld.hello.repository;

import com.HelloWorld.hello.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {}
