package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer> {

    @Query("SELECT u FROM UsuarioModel u JOIN u.roles r WHERE r = 'ROLE_ATENDENTE' AND u.ativo = true")
    List<UsuarioModel> findAtendentesAtivos();

    Optional<UsuarioModel> findByEmail(String email);

    List<UsuarioModel> findByAtivoTrue();

    @Query("SELECT u FROM UsuarioModel u JOIN u.roles r WHERE r = 'ROLE_MECANICO' AND u.ativo = true")
    List<UsuarioModel> findMecanicosAtivos();

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);
}