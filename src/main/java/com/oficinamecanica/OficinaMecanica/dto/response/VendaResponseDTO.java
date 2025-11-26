package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import java.time.LocalDateTime;

public record VendaResponseDTO(
        Integer cdVenda,
        Integer cdCliente,
        String nmCliente,
        Integer cdAtendente,
        String nmAtendente,
        LocalDateTime dataVenda,
        Double vlTotal,
        Double desconto,
        FormaPagamento formaPagamento
) {}