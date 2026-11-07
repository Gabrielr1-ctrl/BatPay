package com.batpay.batpay.controller;

import com.batpay.batpay.dto.PagamentoRequestDTO;
import com.batpay.batpay.dto.PagamentoResponseDTO;
import com.batpay.batpay.model.MetodoPagamento;
import com.batpay.batpay.model.StatusPagamento;
import com.batpay.batpay.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {
    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping
    public List<PagamentoResponseDTO> listar(@RequestParam(required = false) Long cobrancaId,
                                            @RequestParam(required = false) StatusPagamento status,
                                            @RequestParam(required = false) MetodoPagamento metodo) {
        return pagamentoService.listar(cobrancaId, status, metodo);
    }

    @GetMapping("/{id}")
    public PagamentoResponseDTO buscar(@PathVariable UUID id) {
        return pagamentoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> processar(@RequestHeader("Idempotency-Key") String chave,
                                                          @Valid @RequestBody PagamentoRequestDTO dto) {
        var resultado = pagamentoService.processar(dto, chave);
        return ResponseEntity.status(resultado.criado() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(resultado.pagamento());
    }

    @PostMapping("/{id}/estorno")
    public PagamentoResponseDTO estornar(@PathVariable UUID id) {
        return pagamentoService.estornar(id);
    }
}
