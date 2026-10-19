package com.batpay.batpay.service;

import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.model.Usuario;
import com.batpay.batpay.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<UsuarioResponseDTO> listarUsuarios(){
        List<Usuario> aux = new ArrayList<>(usuarioRepository.findAll());
        List<UsuarioResponseDTO> usuariosRetornaveis = new ArrayList<>();

        for (Usuario usuario : aux) {
            UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getDataCadastro()
            );
            usuariosRetornaveis.add(usuarioResponseDTO);
        }
        return usuariosRetornaveis;
    }

    public Optional<Usuario> buscarUsuarioId(Long id){return usuarioRepository.findById(id);}

}
