package com.batpay.batpay.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nome;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(nullable = false, length = 255)
    private String senha;
    private String telefone;
    private String cnpj;
    private String empresa;
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;


    public Usuario() {
    }

    public Usuario(String nome, String email, String senha, String telefone, String cnpj, String empresa, LocalDateTime dataCadastro) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.cnpj = cnpj;
        this.empresa = empresa;
        this.dataCadastro = dataCadastro;
    }

    public Usuario(String nome, String email, String senha, LocalDateTime dataCadastro) {
        this(nome, email, senha, null, null, null, dataCadastro);
    }



    @PrePersist
    public void prePersist(){
        this.dataCadastro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
