package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar por email
    Optional<Usuario> findByEmail(String email);

    // Buscar por email e provider ID (OAuth2)
    Optional<Usuario> findByEmailAndProviderId(String email, String providerId);

    // Buscar por provider ID
    Optional<Usuario> findByProviderId(String providerId);

    // Listar apenas usuários ativos
    List<Usuario> findByAtivoTrue();

    // Listar usuários inativos
    List<Usuario> findByAtivoFalse();

    // Buscar por role específica
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r = :role AND u.ativo = true")
    List<Usuario> findByRole(@Param("role") UserRole role);

    // Buscar mecânicos ativos
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r = 'ROLE_MECANICO' AND u.ativo = true")
    List<Usuario> findMecanicosAtivos();

    // Buscar atendentes ativos
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r = 'ROLE_ATENDENTE' AND u.ativo = true")
    List<Usuario> findAtendentesAtivos();

    // Verificar se email já existe
    boolean existsByEmail(String email);

    // Verificar se CPF já existe
    boolean existsByNuCPF(String nuCPF);

    // Buscar por CPF
    Optional<Usuario> findByNuCPF(String nuCPF);
}
