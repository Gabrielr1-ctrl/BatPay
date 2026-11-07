package com.batpay.batpay.service;

import com.batpay.batpay.dto.ClienteRequestDTO;
import com.batpay.batpay.model.Cliente;
import com.batpay.batpay.repository.ClienteRepository;
import com.batpay.batpay.repository.CobrancaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final CobrancaRepository cobrancaRepository;

    public ClienteService(ClienteRepository clienteRepository, CobrancaRepository cobrancaRepository) {
        this.clienteRepository = clienteRepository;
        this.cobrancaRepository = cobrancaRepository;
    }

    public Cliente salvarCliente(ClienteRequestDTO cliente) {
        return clienteRepository.save(new Cliente(cliente.nome().strip(), cliente.email(), cliente.cpf(), cliente.telefone()));
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional
    public Optional<Cliente> atualizarCliente(Long id, ClienteRequestDTO clienteAtualizado) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setNome(clienteAtualizado.nome().strip());
                    cliente.setEmail(clienteAtualizado.email());
                    cliente.setCpf(clienteAtualizado.cpf());
                    cliente.setTelefone(clienteAtualizado.telefone());
                    return cliente;
                });
    }

    @Transactional
    public boolean deletarCliente(Long id) {
        if (cobrancaRepository.existsByClienteId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O cliente possui cobranças vinculadas.");
        }
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
