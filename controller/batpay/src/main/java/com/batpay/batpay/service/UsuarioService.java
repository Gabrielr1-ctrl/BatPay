package com.batpay.batpay.service;

import com.batpay.batpay.dto.AtualizarSenhaDTO;
import com.batpay.batpay.dto.UsuarioRequestDTO;
import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.model.Usuario;
import com.batpay.batpay.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO salvarUsuario(UsuarioRequestDTO dto) {
        String email = normalizarEmail(dto.email());
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com esse e-mail.");
        }
        Usuario usuario = new Usuario();
        aplicarDados(usuario, dto, email);
        usuario.setSenha(codificarSenha(dto.senha()));
        return toDTO(usuarioRepository.save(usuario));
    }

    public Optional<UsuarioResponseDTO> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::toDTO);
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getCnpj(),
                usuario.getEmpresa(),
                usuario.getDataCadastro()
        );
    }

    @Transactional
    public Optional<UsuarioResponseDTO> atualizaUsuario(Long id, UsuarioRequestDTO dto) {
        String email = normalizarEmail(dto.email());
        return usuarioRepository.buscarParaAlteracao(id)
                .map(usuario -> {
                    if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com esse e-mail.");
                    }
                    aplicarDados(usuario, dto, email);
                    return toDTO(usuario);
                });
    }

    public Optional<UsuarioResponseDTO> autenticarUsuario(String email, String senha) {
        return usuarioRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .filter(usuario -> senhaConfere(senha, usuario))
                .map(this::toDTO);
    }

    @Transactional
    public void atualizarSenha(Long id, AtualizarSenhaDTO dto) {
        Usuario usuario = usuarioRepository.buscarParaAlteracao(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (!senhaConfere(dto.senhaAtual(), usuario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        }
        usuario.setSenha(codificarSenha(dto.novaSenha()));
    }

    private boolean senhaConfere(String senha, Usuario usuario) {
        return senha.getBytes(StandardCharsets.UTF_8).length <= 72 && passwordEncoder.matches(senha, usuario.getSenha());
    }

    private String codificarSenha(String senha) {
        if (senha == null || senha.isBlank() || senha.length() < 6
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 6 caracteres e até 72 bytes.");
        }
        return passwordEncoder.encode(senha);
    }

    private String normalizarEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private void aplicarDados(Usuario usuario, UsuarioRequestDTO dto, String email) {
        usuario.setNome(dto.nome().strip());
        usuario.setEmail(email);
        usuario.setTelefone(dto.telefone());
        usuario.setCnpj(dto.cnpj());
        usuario.setEmpresa(dto.empresa());
    }
}
