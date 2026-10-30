package com.batpay.batpay.service;

import com.batpay.batpay.dto.UsuarioResponseDTO;
import com.batpay.batpay.model.Usuario;
import com.batpay.batpay.repository.UsuarioRepository;
import org.jspecify.annotations.NonNull;
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
        List<Usuario> aux = usuarioRepository.findAll();
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


    public Optional<UsuarioResponseDTO> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> new UsuarioResponseDTO(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getDataCadastro()
                ));
    }

    public boolean verificarUsuarioValido(Long id){
        return usuarioRepository.findById(id)
                .map(usuario -> true).orElse(false);
    }

    public boolean verificarSenha(Long id, String senhaDigitada){
        Optional<Usuario> usuarioArmazenado = usuarioRepository.findById(id);
        return verificarUsuarioValido(usuarioArmazenado.get().getId()) && passwordEncoder.matches(senhaDigitada, usuarioArmazenado.get().getSenha());
    }

    public UsuarioResponseDTO transformaUsuarioEmUsuarioResponseDTO( Usuario usuario){
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDataCadastro()
        );

    }

    public Optional<UsuarioResponseDTO> atualizaUsuario(Long id, Usuario usuarioAtualizado){
         Optional<Usuario> usuarioDesatualizado = usuarioRepository.findById(id);
         usuarioDesatualizado.map(usuario -> {
             usuario.setNome(usuarioAtualizado.getNome());
             usuario.setEmail(usuarioAtualizado.getEmail());
             usuario.setSenha(usuarioAtualizado.getSenha());
             return transformaUsuarioEmUsuarioResponseDTO(usuario);
         });

         return Optional.empty();

    }




}
