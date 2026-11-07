package com.batpay.batpay.service;

import com.batpay.batpay.dto.PagamentoRequestDTO;
import com.batpay.batpay.dto.PagamentoResponseDTO;
import com.batpay.batpay.model.*;
import com.batpay.batpay.repository.CobrancaRepository;
import com.batpay.batpay.repository.PagamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PagamentoService {
    private final CobrancaService cobrancaService;
    private final CobrancaRepository cobrancaRepository;
    private final PagamentoRepository pagamentoRepository;

    public PagamentoService(CobrancaService cobrancaService, CobrancaRepository cobrancaRepository,
                            PagamentoRepository pagamentoRepository) {
        this.cobrancaService = cobrancaService;
        this.cobrancaRepository = cobrancaRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    public List<PagamentoResponseDTO> listar(Long cobrancaId, StatusPagamento status, MetodoPagamento metodo) {
        return pagamentoRepository.listar(cobrancaId, status, metodo).stream().map(this::toDTO).toList();
    }

    public PagamentoResponseDTO buscar(UUID id) {
        return toDTO(buscarPagamento(id));
    }

    @Transactional
    public Resultado processar(PagamentoRequestDTO dto, String chave) {
        if (!chave.matches("[a-zA-Z0-9._-]{1,100}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key inválida.");
        }
        String chaveNormalizada = chave.toLowerCase(Locale.ROOT);
        Cobranca cobranca = cobrancaService.buscarParaAlteracao(dto.cobrancaId());
        boolean aprovar = dto.resultado() == PagamentoRequestDTO.ResultadoSimulado.APROVADO;
        var existente = pagamentoRepository.findByCobrancaIdAndChaveIdempotencia(cobranca.getId(), chaveNormalizada);
        if (existente.isPresent()) {
            Pagamento pagamento = existente.get();
            boolean aprovou = pagamento.getStatus() != StatusPagamento.RECUSADO;
            if (aprovou != aprovar) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A chave já foi usada com outro resultado.");
            }
            return new Resultado(toDTO(pagamento), false);
        }
        if (cobranca.statusEm(Cobranca.hoje()) != StatusCobranca.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente cobranças pendentes podem receber pagamentos.");
        }
        MetodoPagamento metodo;
        try {
            metodo = MetodoPagamento.interpretar(cobranca.getMetodoPagamento());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A cobrança não possui um método de pagamento válido.");
        }
        StatusPagamento status = aprovar ? StatusPagamento.APROVADO : StatusPagamento.RECUSADO;
        Pagamento pagamento = pagamentoRepository.save(new Pagamento(cobranca, chaveNormalizada, metodo, status));
        if (aprovar) {
            cobranca.setStatus(StatusCobranca.PAGO);
        }
        return new Resultado(toDTO(pagamento), true);
    }

    @Transactional
    public PagamentoResponseDTO estornar(UUID id) {
        Cobranca cobranca = cobrancaRepository.buscarPeloPagamentoParaAlteracao(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
        Pagamento pagamento = buscarPagamento(id);
        if (pagamento.getStatus() == StatusPagamento.RECUSADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente pagamentos aprovados podem ser estornados.");
        }
        if (pagamento.getStatus() == StatusPagamento.APROVADO) {
            pagamento.estornar();
            cobranca.setStatus(StatusCobranca.ESTORNADO);
        }
        return toDTO(pagamento);
    }

    private Pagamento buscarPagamento(UUID id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
    }

    private PagamentoResponseDTO toDTO(Pagamento pagamento) {
        return new PagamentoResponseDTO(pagamento.getId(), pagamento.getCobranca().getId(),
                pagamento.getMetodo(), pagamento.getValor(), "BRL", pagamento.getStatus(),
                pagamento.getCriadoEm(), pagamento.getEstornadoEm(), true);
    }

    public record Resultado(PagamentoResponseDTO pagamento, boolean criado) {}
}
