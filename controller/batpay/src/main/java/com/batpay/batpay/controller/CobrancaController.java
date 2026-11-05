package com.batpay.batpay.controller;

import com.batpay.batpay.dto.CobrancaRequestDTO;
import com.batpay.batpay.dto.CobrancaResponseDTO;
import com.batpay.batpay.model.StatusCobranca;
import com.batpay.batpay.service.CobrancaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cobrancas")
@CrossOrigin("*")
public class CobrancaController {

    private final CobrancaService cobrancaService;

    public CobrancaController(CobrancaService cobrancaService) {
        this.cobrancaService = cobrancaService;
    }

    @GetMapping
    public List<CobrancaResponseDTO> listar(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            StatusCobranca s = StatusCobranca.valueOf(status.toUpperCase());
            return cobrancaService.listarPorStatus(s);
        }
        return cobrancaService.listarCobrancas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaResponseDTO> buscarPorId(@PathVariable Long id) {
        return cobrancaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody CobrancaRequestDTO dto) {
        try {
            CobrancaResponseDTO cobrancaCriada = cobrancaService.criarCobranca(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(cobrancaCriada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (cobrancaService.deletarCobranca(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
