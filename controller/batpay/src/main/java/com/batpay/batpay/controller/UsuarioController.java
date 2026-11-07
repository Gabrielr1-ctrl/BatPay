package com.batpay.batpay.controller;

import com.batpay.batpay.dto.AtualizarSenhaDTO;
import com.batpay.batpay.dto.LoginRequestDTO;
import com.batpay.batpay.dto.UsuarioRequestDTO;
import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static com.batpay.batpay.config.CorsConfig.USUARIO_LOGADO;

@RequestMapping("/usuarios")
@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> listarUsuarios(@SessionAttribute(USUARIO_LOGADO) Long usuarioLogado) {
        return usuarioService.buscarUsuarioPorId(usuarioLogado).stream().toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO criarUsuario(@Valid @RequestBody UsuarioRequestDTO novoUsuario) {
        return usuarioService.salvarUsuario(novoUsuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id,
                                                         @SessionAttribute(USUARIO_LOGADO) Long usuarioLogado) {
        exigirProprioUsuario(id, usuarioLogado);
        return usuarioService.buscarUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizarPorId(@PathVariable Long id,
                                                            @SessionAttribute(USUARIO_LOGADO) Long usuarioLogado,
                                                            @Valid @RequestBody UsuarioRequestDTO usuario) {
        exigirProprioUsuario(id, usuarioLogado);
        return usuarioService.atualizaUsuario(id, usuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public UsuarioResponseDTO login(@Valid @RequestBody LoginRequestDTO dto, HttpServletRequest request) {
        UsuarioResponseDTO usuario = usuarioService.autenticarUsuario(dto.email(), dto.senha())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        request.getSession().setAttribute(USUARIO_LOGADO, usuario.id());
        return usuario;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession sessao) {
        sessao.invalidate();
    }

    @PutMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarSenha(@PathVariable Long id, @SessionAttribute(USUARIO_LOGADO) Long usuarioLogado,
                               @Valid @RequestBody AtualizarSenhaDTO dto, HttpSession sessao) {
        exigirProprioUsuario(id, usuarioLogado);
        usuarioService.atualizarSenha(id, dto);
        sessao.invalidate();
    }

    private void exigirProprioUsuario(Long id, Long usuarioLogado) {
        if (!Objects.equals(id, usuarioLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar sua própria conta.");
        }
    }
}
