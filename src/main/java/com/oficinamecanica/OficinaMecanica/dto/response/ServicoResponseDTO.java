package com.oficinamecanica.OficinaMecanica.dto.response;

public record ServicoResponseDTO(
        Integer cdServico,
        String nmServico,
        String dsServico,
        Double vlServico,
        Boolean ativo
) {}