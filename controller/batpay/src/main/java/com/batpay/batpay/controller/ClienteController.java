package com.batpay.batpay.controller;

import com.batpay.batpay.model.Cliente;
import com.batpay.batpay.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping ("/clientes")
@RestController
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> listarClientes(){
        return clienteService.listarClientes();
    }

    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente novoCliente){
        Cliente clienteSalvo = clienteService.salvarCliente(novoCliente);
        return new ResponseEntity<>(clienteSalvo, HttpStatus.CREATED);
    }

}
