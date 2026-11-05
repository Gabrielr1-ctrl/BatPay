package com.batpay.batpay.repository;

import com.batpay.batpay.model.Cobranca;
import com.batpay.batpay.model.StatusCobranca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {
    List<Cobranca> findByStatus(StatusCobranca status);
}
