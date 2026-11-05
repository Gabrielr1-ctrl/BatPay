package com.batpay.batpay.controller;

import com.batpay.batpay.dto.LoginRequestDTO;
import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.model.Usuario;
import com.batpay.batpay.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RequestMapping("/usuarios")
@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody Usuario novoUsuario) {
        Usuario usuarioSalvo = usuarioService.salvarUsuario(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.transformaUsuarioEmUsuarioResponseDTO(usuarioSalvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizarPorId(@PathVariable Long id, @RequestBody Usuario usuario) {
        return usuarioService.atualizaUsuario(id, usuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        return usuarioService.autenticarUsuario(dto.email(), dto.senha())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos."));
    }
}
