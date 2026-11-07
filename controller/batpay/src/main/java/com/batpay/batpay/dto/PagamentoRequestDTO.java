package com.batpay.batpay.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagamentoRequestDTO(
        @NotNull @Positive Long cobrancaId,
        @NotNull ResultadoSimulado resultado
) {
    public enum ResultadoSimulado {
        APROVADO,
        RECUSADO
    }
}
