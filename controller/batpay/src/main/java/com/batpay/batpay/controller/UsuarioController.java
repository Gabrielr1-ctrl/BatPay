package com.batpay.batpay.controller;

import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.model.Usuario;
import com.batpay.batpay.repository.UsuarioRepository;
import com.batpay.batpay.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RequestMapping ("/usuarios")
@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.transformaUsuarioEmUsuarioResponseDTO(usuarioSalvo));
    }

    @GetMapping ("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(@PathVariable Long id){
        return ResponseEntity.of(usuarioService.buscarUsuarioPorId(id));
    }



    @PutMapping ("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizarPorId(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado, @RequestBody String senhaDigitada){

        if (usuarioService.verificarSenha(id, senhaDigitada)){

            return ResponseEntity.of(usuarioService.atualizaUsuario(id,usuarioAtualizado));
        }else{

        }

    }

}
