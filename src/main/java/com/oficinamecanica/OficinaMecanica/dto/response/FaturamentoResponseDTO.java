package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import java.time.LocalDateTime;

public record FaturamentoResponseDTO(
        Integer cdFaturamento,
        Integer cdVenda,
        Integer cdOrdemServico,
        LocalDateTime dataVenda,
        Double vlTotal,
        FormaPagamento formaPagamento,
        String nomeCliente,
        String tipoTransacao
) {}