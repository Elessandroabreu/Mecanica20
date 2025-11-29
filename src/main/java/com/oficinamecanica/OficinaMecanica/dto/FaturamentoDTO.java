package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;

public record FaturamentoDTO(
        Integer cdFaturamento,
        Integer cdVenda,
        Integer cdOrdemServico,
        String dataVenda,
        Double vlTotal,
        FormaPagamento formaPagamento
) {}