package com.batpay.batpay.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String cnpj,
        String empresa,
        LocalDateTime dataCadastro
) {}
