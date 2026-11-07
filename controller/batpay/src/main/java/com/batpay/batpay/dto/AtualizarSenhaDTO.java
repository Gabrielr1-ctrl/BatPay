package com.batpay.batpay.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarSenhaDTO(@NotBlank String senhaAtual, @NotBlank String novaSenha) {}
