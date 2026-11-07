package com.batpay.batpay.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<String> erroDaOperacao(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(erro.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<String> erroDeValidacao(MethodArgumentNotValidException erro) {
        var campo = erro.getFieldError();
        String mensagem = campo == null ? "Dados inválidos." : "Campo inválido: " + campo.getField() + ".";
        return ResponseEntity.badRequest().body(mensagem);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ServletRequestBindingException.class})
    ResponseEntity<String> requisicaoInvalida() {
        return ResponseEntity.badRequest().body("Confira o formato dos campos, parâmetros e cabeçalhos.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<String> conflitoDeDados() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("O registro já existe ou está vinculado a outro registro.");
    }
}
