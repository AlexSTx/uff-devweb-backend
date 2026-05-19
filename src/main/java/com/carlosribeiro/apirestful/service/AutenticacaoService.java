package com.carlosribeiro.apirestful.service;

import com.carlosribeiro.apirestful.model.Usuario;
import com.carlosribeiro.apirestful.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario login(Usuario usuario) {
        // System.out.println("Conta = " + usuario.getConta() + " e senha = " + usuario.getSenha());
        return usuarioRepository.findByContaAndSenha(
            usuario.getConta(), usuario.getSenha());
    }
}
