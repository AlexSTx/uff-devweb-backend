package com.carlosribeiro.apirestful.repository;

import com.carlosribeiro.apirestful.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByContaAndSenha(String conta, String senha);

    Usuario findByConta(String conta);
}
