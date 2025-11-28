package com.oficinamecanica.OficinaMecanica.dto;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        UsuarioDTO usuario
) {}