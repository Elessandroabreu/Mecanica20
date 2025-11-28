package com.oficinamecanica.OficinaMecanica.dto;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        UsuarioResponseDTO usuario  // ← CORRIGIDO
) {}