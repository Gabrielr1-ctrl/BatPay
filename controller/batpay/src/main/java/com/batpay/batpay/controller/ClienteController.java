package com.batpay.batpay.controller;

import com.batpay.batpay.model.Cliente;
import com.batpay.batpay.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RequestMapping("/clientes")
@RestController
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> listarClientes() {
        return clienteService.listarClientes();
    }

    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente novoCliente) {
        Cliente clienteSalvo = clienteService.salvarCliente(novoCliente);
        return new ResponseEntity<>(clienteSalvo, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarClientePorId(@PathVariable Long id) {
        return clienteService.buscarClientePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizarCliente(@PathVariable Long id, @RequestBody Cliente clienteAtualizado) {
        return clienteService.atualizarCliente(id, clienteAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
        if (clienteService.deletarCliente(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
