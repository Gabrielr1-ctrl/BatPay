package com.batpay.batpay.dto;

import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        String senha,
        @Size(max = 255) String telefone,
        @Size(max = 255) String cnpj,
        @Size(max = 255) String empresa
) {}
