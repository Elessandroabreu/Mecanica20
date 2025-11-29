package com.oficinamecanica.OficinaMecanica.dto;

public record AuthResponseDTO(
        String accessToken,  // ✅ CORRIGIDO: era "token"
        String tokenType,    // ✅ CORRIGIDO: era "type"
        UsuarioResponseDTO usuario
) {}


