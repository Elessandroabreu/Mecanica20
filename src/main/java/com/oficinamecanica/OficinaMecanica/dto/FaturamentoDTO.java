package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import java.time.LocalDateTime;

public record FaturamentoDTO(
        Integer cdFaturamento,
        Integer cdVenda,
        Integer cdOrdemServico,
        LocalDateTime dataVenda,
        Double vlTotal,
        FormaPagamento formaPagamento
) {}