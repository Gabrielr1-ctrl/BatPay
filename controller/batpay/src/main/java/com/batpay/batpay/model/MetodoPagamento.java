package com.batpay.batpay.model;

import java.util.Locale;

public enum MetodoPagamento {
    PIX,
    CARTAO,
    BOLETO;

    public static MetodoPagamento interpretar(String valor) {
        return valueOf(valor == null ? "" : valor.strip().toUpperCase(Locale.ROOT));
    }
}
