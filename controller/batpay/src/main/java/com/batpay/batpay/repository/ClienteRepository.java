package com.batpay.batpay.repository;

import com.batpay.batpay.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ClienteRepository extends JpaRepository<Cliente, Long> {

}
