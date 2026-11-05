package com.batpay.batpay.service;

import com.batpay.batpay.dto.CobrancaRequestDTO;
import com.batpay.batpay.dto.CobrancaResponseDTO;
import com.batpay.batpay.model.Cliente;
import com.batpay.batpay.model.Cobranca;
import com.batpay.batpay.model.StatusCobranca;
import com.batpay.batpay.repository.ClienteRepository;
import com.batpay.batpay.repository.CobrancaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final ClienteRepository clienteRepository;

    public CobrancaService(CobrancaRepository cobrancaRepository, ClienteRepository clienteRepository) {
        this.cobrancaRepository = cobrancaRepository;
        this.clienteRepository = clienteRepository;
    }

    public CobrancaResponseDTO criarCobranca(CobrancaRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + dto.clienteId()));

        Cobranca cobranca = new Cobranca();
        cobranca.setCliente(cliente);
        cobranca.setDescricao(dto.descricao());
        cobranca.setValor(dto.valor());
        cobranca.setDataVencimento(LocalDate.parse(dto.dataVencimento()));
        cobranca.setMetodoPagamento(dto.metodoPagamento());
        cobranca.setStatus(StatusCobranca.PENDENTE);

        Cobranca salva = cobrancaRepository.save(cobranca);
        return toDTO(salva);
    }

    public List<CobrancaResponseDTO> listarCobrancas() {
        return cobrancaRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<CobrancaResponseDTO> listarPorStatus(StatusCobranca status) {
        return cobrancaRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<CobrancaResponseDTO> buscarPorId(Long id) {
        return cobrancaRepository.findById(id)
                .map(this::toDTO);
    }

    public boolean deletarCobranca(Long id) {
        if (cobrancaRepository.existsById(id)) {
            cobrancaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CobrancaResponseDTO toDTO(Cobranca cobranca) {
        return new CobrancaResponseDTO(
                cobranca.getId(),
                cobranca.getCliente().getId(),
                cobranca.getCliente().getNome(),
                cobranca.getDescricao(),
                cobranca.getValor(),
                cobranca.getDataVencimento(),
                cobranca.getMetodoPagamento(),
                cobranca.getStatus(),
                cobranca.getCriadoEm()
        );
    }
}
