package com.oficinamecanica.OficinaMecanica.dto.response;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Integer cdCliente,
        String nmCliente,
        String nuCPF,
        String nuTelefone,
        String dsEndereco,
        String email,
        Boolean ativo,
        LocalDateTime dataCadastro
) {}