package com.batpay.batpay.service;

import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.model.Usuario;
import com.batpay.batpay.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario salvarUsuario(Usuario novoUsuario) {
        String senhaHash = passwordEncoder.encode(novoUsuario.getSenha());
        novoUsuario.setSenha(senhaHash);

        return usuarioRepository.save(novoUsuario);
    }

    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::transformaUsuarioEmUsuarioResponseDTO)
                .toList();
    }

    public Optional<UsuarioResponseDTO> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::transformaUsuarioEmUsuarioResponseDTO);
    }

    public boolean verificarUsuarioValido(Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> true).orElse(false);
    }

    public boolean verificarSenha(Long id, String senhaDigitada) {
        return usuarioRepository.findById(id)
                .map(usuario -> passwordEncoder.matches(senhaDigitada, usuario.getSenha()))
                .orElse(false);
    }

    public UsuarioResponseDTO transformaUsuarioEmUsuarioResponseDTO(Usuario usuario) {
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

    public Optional<UsuarioResponseDTO> atualizaUsuario(Long id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNome(usuarioAtualizado.getNome());
                    usuario.setEmail(usuarioAtualizado.getEmail());
                    usuario.setTelefone(usuarioAtualizado.getTelefone());
                    usuario.setCnpj(usuarioAtualizado.getCnpj());
                    usuario.setEmpresa(usuarioAtualizado.getEmpresa());
                    Usuario salvo = usuarioRepository.save(usuario);
                    return transformaUsuarioEmUsuarioResponseDTO(salvo);
                });
    }

    public Optional<UsuarioResponseDTO> autenticarUsuario(String email, String senha) {
        return usuarioRepository.findByEmail(email)
                .filter(usuario -> passwordEncoder.matches(senha, usuario.getSenha()))
                .map(this::transformaUsuarioEmUsuarioResponseDTO);
    }
}
