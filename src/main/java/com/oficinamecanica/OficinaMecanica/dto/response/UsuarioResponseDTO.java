package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioResponseDTO(
        Integer cdUsuario,
        String nmUsuario,
        String email,
        AuthProvider provider,
        Set<UserRole> roles,
        String nuTelefone,
        String nuCPF,
        Boolean ativo,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {}