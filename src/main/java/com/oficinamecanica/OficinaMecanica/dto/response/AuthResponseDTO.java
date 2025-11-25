package com.oficinamecanica.OficinaMecanica.dto.response;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        UsuarioResponseDTO usuario
) {}