package com.batpay.batpay.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "pagamentos", uniqueConstraints = @UniqueConstraint(
        name = "uk_pagamento_cobranca_chave", columnNames = {"cobranca_id", "chave_idempotencia"}))
public class Pagamento {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cobranca_id", nullable = false)
    private Cobranca cobranca;

    @Column(name = "chave_idempotencia", nullable = false, length = 100)
    private String chaveIdempotencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(nullable = false)
    private Instant criadoEm;

    private Instant estornadoEm;

    protected Pagamento() {
    }

    public Pagamento(Cobranca cobranca, String chaveIdempotencia, MetodoPagamento metodo,
                     StatusPagamento status) {
        this.id = UUID.randomUUID();
        this.cobranca = cobranca;
        this.chaveIdempotencia = chaveIdempotencia;
        this.metodo = metodo;
        this.valor = cobranca.getValor();
        this.status = status;
        this.criadoEm = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public void estornar() {
        status = StatusPagamento.ESTORNADO;
        estornadoEm = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public UUID getId() { return id; }
    public Cobranca getCobranca() { return cobranca; }
    public MetodoPagamento getMetodo() { return metodo; }
    public BigDecimal getValor() { return valor; }
    public StatusPagamento getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getEstornadoEm() { return estornadoEm; }
}
