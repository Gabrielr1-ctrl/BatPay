package com.batpay.batpay.dto;

import com.batpay.batpay.model.MetodoPagamento;
import com.batpay.batpay.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagamentoResponseDTO(
        UUID id,
        Long cobrancaId,
        MetodoPagamento metodo,
        BigDecimal valor,
        String moeda,
        StatusPagamento status,
        Instant criadoEm,
        Instant estornadoEm,
        boolean simulado
) {}
