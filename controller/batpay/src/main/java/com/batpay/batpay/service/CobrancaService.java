package com.batpay.batpay.service;

import com.batpay.batpay.dto.CobrancaRequestDTO;
import com.batpay.batpay.dto.CobrancaResponseDTO;
import com.batpay.batpay.model.Cliente;
import com.batpay.batpay.model.Cobranca;
import com.batpay.batpay.model.MetodoPagamento;
import com.batpay.batpay.model.StatusCobranca;
import com.batpay.batpay.repository.ClienteRepository;
import com.batpay.batpay.repository.CobrancaRepository;
import com.batpay.batpay.repository.PagamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoRepository pagamentoRepository;

    public CobrancaService(CobrancaRepository cobrancaRepository, ClienteRepository clienteRepository,
                           PagamentoRepository pagamentoRepository) {
        this.cobrancaRepository = cobrancaRepository;
        this.clienteRepository = clienteRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional
    public CobrancaResponseDTO criarCobranca(CobrancaRequestDTO dto) {
        LocalDate hoje = Cobranca.hoje();
        if (dto.dataVencimento().isBefore(hoje)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de vencimento já passou.");
        }
        MetodoPagamento metodo;
        try {
            metodo = MetodoPagamento.interpretar(dto.metodoPagamento());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use PIX, CARTAO ou BOLETO.");
        }
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        Cobranca cobranca = new Cobranca(cliente, dto.descricao().strip(), dto.valor(),
                dto.dataVencimento(), metodo.name(), StatusCobranca.PENDENTE);
        return toDTO(cobrancaRepository.save(cobranca), hoje);
    }

    public List<CobrancaResponseDTO> listarCobrancas(String filtro) {
        LocalDate hoje = Cobranca.hoje();
        List<Cobranca> cobrancas;
        if (filtro == null || filtro.isBlank()) {
            cobrancas = cobrancaRepository.findAll();
        } else {
            StatusCobranca status;
            try {
                status = StatusCobranca.valueOf(filtro.strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de cobrança inválido.");
            }
            cobrancas = switch (status) {
                case PENDENTE -> cobrancaRepository.findByStatusAndDataVencimentoGreaterThanEqual(status, hoje);
                case VENCIDO -> cobrancaRepository.findByStatusOrStatusAndDataVencimentoLessThan(
                        status, StatusCobranca.PENDENTE, hoje);
                default -> cobrancaRepository.findByStatus(status);
            };
        }
        return cobrancas.stream().map(cobranca -> toDTO(cobranca, hoje)).toList();
    }

    public Optional<CobrancaResponseDTO> buscarPorId(Long id) {
        return cobrancaRepository.findById(id)
                .map(cobranca -> toDTO(cobranca, Cobranca.hoje()));
    }

    @Transactional
    public CobrancaResponseDTO cancelar(Long id) {
        Cobranca cobranca = buscarParaAlteracao(id);
        if (cobranca.getStatus() == StatusCobranca.PAGO || cobranca.getStatus() == StatusCobranca.ESTORNADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A cobrança possui um pagamento aprovado.");
        }
        cobranca.setStatus(StatusCobranca.CANCELADO);
        return toDTO(cobranca, Cobranca.hoje());
    }

    @Transactional
    public void deletarCobranca(Long id) {
        Cobranca cobranca = buscarParaAlteracao(id);
        if (cobranca.getStatus() == StatusCobranca.PAGO || cobranca.getStatus() == StatusCobranca.ESTORNADO
                || pagamentoRepository.existsByCobrancaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A cobrança possui histórico de pagamentos.");
        }
        cobrancaRepository.delete(cobranca);
    }

    Cobranca buscarParaAlteracao(Long id) {
        return cobrancaRepository.buscarParaAlteracao(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cobrança não encontrada."));
    }

    private CobrancaResponseDTO toDTO(Cobranca cobranca, LocalDate hoje) {
        return new CobrancaResponseDTO(
                cobranca.getId(),
                cobranca.getCliente().getId(),
                cobranca.getCliente().getNome(),
                cobranca.getDescricao(),
                cobranca.getValor(),
                cobranca.getDataVencimento(),
                cobranca.getMetodoPagamento(),
                cobranca.statusEm(hoje),
                cobranca.getCriadoEm()
        );
    }
}
