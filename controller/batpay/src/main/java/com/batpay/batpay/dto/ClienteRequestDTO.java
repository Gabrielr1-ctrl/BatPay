package com.batpay.batpay.dto;

import jakarta.validation.constraints.*;

public record ClienteRequestDTO(
        @NotBlank @Size(max = 255) String nome,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String cpf,
        @Size(max = 255) String telefone
) {}
