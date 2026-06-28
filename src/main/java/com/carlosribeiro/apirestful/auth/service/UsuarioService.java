package com.carlosribeiro.apirestful.auth.service;

import com.carlosribeiro.apirestful.auth.dto.RedefinirSenhaRequest;
import com.carlosribeiro.apirestful.auth.dto.UsuarioCreate;
import com.carlosribeiro.apirestful.auth.model.Usuario;
import com.carlosribeiro.apirestful.auth.repository.UsuarioRepository;
import com.carlosribeiro.apirestful.auth.util.InfoRedefinicaoSenha;
import com.carlosribeiro.apirestful.auth.util.InfoUsuario;
import com.carlosribeiro.apirestful.auth.util.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public InfoUsuario cadastrarUsuario(UsuarioCreate usuarioCreate) {
        Usuario usuarioCadastrado = usuarioRepository
            .findByEmail(usuarioCreate.getEmail())
            .orElse(null);
        if (usuarioCadastrado == null) {
            Usuario usuario = new Usuario(
                usuarioCreate.getNome(),
                usuarioCreate.getEmail(),
                passwordEncoder.encode(usuarioCreate.getSenha()),
                Role.USER);
            usuarioRepository.save(usuario);
            return new InfoUsuario(true, false, "Usuário cadastrado com sucesso!");
        }
        else {
            return new InfoUsuario(false, true, "Email já cadastrado!");
        }
    }

    // Redefinição de senha simples (sem email/token): o usuário informa o
    // email e a nova senha, e a senha é trocada diretamente.
    public InfoRedefinicaoSenha redefinirSenha(RedefinirSenhaRequest request) {
        Usuario usuario = usuarioRepository
            .findByEmail(request.email())
            .orElse(null);
        if (usuario == null) {
            return new InfoRedefinicaoSenha(false, "Email não cadastrado.");
        }
        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
        return new InfoRedefinicaoSenha(true, "Senha redefinida com sucesso!");
    }

    public List<Usuario> recuperarUsuarios() {
        return usuarioRepository.findAll();
    }
}
