package com.batpay.batpay.controller;

import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.model.Usuario;
import com.batpay.batpay.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping ("/usuarios")
@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> listarUsuarios(){return usuarioService.listarUsuarios();}

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody Usuario novoUsuario){
        Usuario usuarioSalvo = usuarioService.salvarUsuario(novoUsuario);
        UsuarioResponseDTO resposta = new UsuarioResponseDTO(
                usuarioSalvo.getId(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getDataCadastro()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }


}
