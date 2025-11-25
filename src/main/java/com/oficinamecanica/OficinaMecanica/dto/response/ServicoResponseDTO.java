package com.oficinamecanica.OficinaMecanica.dto.response;

import java.time.LocalDateTime;

public record ServicoResponseDTO(
        Integer cdServico,
        String nmServico,
        String dsServico,
        Double vlServico,
        Integer tmpEstimado,
        Boolean ativo,
        LocalDateTime dataCadastro
) {}