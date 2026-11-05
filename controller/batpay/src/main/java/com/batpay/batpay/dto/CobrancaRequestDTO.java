package com.batpay.batpay.dto;

import java.math.BigDecimal;

public record CobrancaRequestDTO(
    Long clienteId,
    String descricao,
    BigDecimal valor,
    String dataVencimento,
    String metodoPagamento
) {}
