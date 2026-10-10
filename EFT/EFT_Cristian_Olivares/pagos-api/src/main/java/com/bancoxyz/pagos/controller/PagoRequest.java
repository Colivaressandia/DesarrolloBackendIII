package com.bancoxyz.pagos.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagoRequest(
        @NotNull @Positive Long cuentaOrigen,
        @NotNull @Positive Long cuentaDestino,
        @NotNull @DecimalMin("0.01") BigDecimal monto,
        @NotNull @Pattern(regexp = "[A-Z]{3}") String moneda,
        @NotNull @Size(min = 1, max = 80) String claveIdempotencia) {
}
