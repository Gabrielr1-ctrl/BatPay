package com.batpay.batpay.dto;

import jakarta.validation.constraints.*;

public record LoginRequestDTO(@NotBlank @Email @Size(max = 150) String email, @NotBlank String senha) {
}
