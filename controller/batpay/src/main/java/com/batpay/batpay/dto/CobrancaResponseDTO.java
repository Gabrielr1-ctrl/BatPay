package com.batpay.batpay.dto;

import com.batpay.batpay.model.StatusCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CobrancaResponseDTO(
    Long id,
    Long clienteId,
    String clienteNome,
    String descricao,
    BigDecimal valor,
    LocalDate dataVencimento,
    String metodoPagamento,
    StatusCobranca status,
    LocalDateTime criadoEm
) {}
