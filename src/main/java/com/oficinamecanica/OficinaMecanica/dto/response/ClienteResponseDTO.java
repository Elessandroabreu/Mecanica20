package com.oficinamecanica.OficinaMecanica.dto.response;

public record ClienteResponseDTO(
        Integer cdCliente,
        String nmCliente,
        String nuCPF,
        String nuTelefone,
        String dsEndereco,
        String email,
        Boolean ativo
) {}