package com.oficinamecanica.OficinaMecanica.dto;


public record AuthResponseDTO(
        String token,
        String type,  // Sempre "Bearer"
        UsuarioResponseDTO usuario
) {}








