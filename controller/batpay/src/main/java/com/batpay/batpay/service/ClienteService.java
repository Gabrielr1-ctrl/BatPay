package com.batpay.batpay.service;

import com.batpay.batpay.model.Cliente;
import com.batpay.batpay.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente salvarCliente(Cliente cliente){
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes(){
        return clienteRepository.findAll();
    }
    public Optional<Cliente> buscarClientePorId(Long id){
        return clienteRepository.findById(id);
    }

}
