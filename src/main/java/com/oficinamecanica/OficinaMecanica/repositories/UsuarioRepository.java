package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // No UsuarioRepository
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r = 'ROLE_ATENDENTE' AND u.ativo = true")
    List<Usuario> findAtendentesAtivos();

    // Buscar por email
    Optional<Usuario> findByEmail(String email);

    // Listar apenas usuários ativos
    List<Usuario> findByAtivoTrue();

    // Buscar mecânicos ativos
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r = 'ROLE_MECANICO' AND u.ativo = true")
    List<Usuario> findMecanicosAtivos();

    // Verificar se email já existe
    boolean existsByEmail(String email);

    // Verificar se CPF já existe
    boolean existsByNuCPF(String nuCPF);
}