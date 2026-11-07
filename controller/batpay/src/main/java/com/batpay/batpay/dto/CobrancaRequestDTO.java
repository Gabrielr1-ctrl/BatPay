package com.batpay.batpay.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobrancaRequestDTO(
    @NotNull @Positive Long clienteId,
    @NotBlank @Size(max = 255) String descricao,
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal valor,
    @NotNull LocalDate dataVencimento,
    @NotBlank String metodoPagamento
) {}
