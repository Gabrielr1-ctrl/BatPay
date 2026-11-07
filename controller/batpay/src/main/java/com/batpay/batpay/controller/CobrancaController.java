package com.batpay.batpay.controller;

import com.batpay.batpay.dto.CobrancaRequestDTO;
import com.batpay.batpay.dto.CobrancaResponseDTO;
import com.batpay.batpay.service.CobrancaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cobrancas")
public class CobrancaController {

    private final CobrancaService cobrancaService;

    public CobrancaController(CobrancaService cobrancaService) {
        this.cobrancaService = cobrancaService;
    }

    @GetMapping
    public List<CobrancaResponseDTO> listar(@RequestParam(required = false) String status) {
        return cobrancaService.listarCobrancas(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaResponseDTO> buscarPorId(@PathVariable Long id) {
        return cobrancaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CobrancaResponseDTO criar(@Valid @RequestBody CobrancaRequestDTO dto) {
        return cobrancaService.criarCobranca(dto);
    }

    @PostMapping("/{id}/cancelamento")
    public CobrancaResponseDTO cancelar(@PathVariable Long id) {
        return cobrancaService.cancelar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cobrancaService.deletarCobranca(id);
        return ResponseEntity.noContent().build();
    }
}
