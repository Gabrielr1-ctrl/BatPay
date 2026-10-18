package com.batpay.batpay.service;

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

    public Usuario salvarUsuario(Usuario novoUsuario){
        String senhaHash = passwordEncoder.encode(novoUsuario.getSenha());
        novoUsuario.setSenha(senhaHash);

        return usuarioRepository.save(novoUsuario);
    }

    public List<Usuario> listarUsuarios(){return usuarioRepository.findAll();}

    public Optional<Usuario> buscarUsuarioId(Long id){return usuarioRepository.findById(id);}

}
