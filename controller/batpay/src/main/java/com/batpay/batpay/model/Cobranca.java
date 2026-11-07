package com.batpay.batpay.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "cobrancas")
public class Cobranca {

    public static LocalDate hoje() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    }

    public StatusCobranca statusEm(LocalDate data) {
        if (status == StatusCobranca.PENDENTE && dataVencimento.isBefore(data)) {
            return StatusCobranca.VENCIDO;
        }
        return status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "metodo_pagamento")
    private String metodoPagamento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private StatusCobranca status;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    public Cobranca() {
    }

    public Cobranca(Cliente cliente, String descricao, BigDecimal valor, LocalDate dataVencimento, String metodoPagamento, StatusCobranca status) {
        this.cliente = cliente;
        this.descricao = descricao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.metodoPagamento = metodoPagamento;
        this.status = status;
    }

    public Cobranca(Long id, Cliente cliente, String descricao, BigDecimal valor, LocalDate dataVencimento, String metodoPagamento, StatusCobranca status, LocalDateTime criadoEm) {
        this.id = id;
        this.cliente = cliente;
        this.descricao = descricao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.metodoPagamento = metodoPagamento;
        this.status = status;
        this.criadoEm = criadoEm;
    }

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusCobranca.PENDENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public StatusCobranca getStatus() {
        return status;
    }

    public void setStatus(StatusCobranca status) {
        this.status = status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
