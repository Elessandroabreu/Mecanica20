// VendaResponseDTO.java
package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;

public record VendaResponseDTO(
        Integer cdVenda,
        Integer cdCliente,
        String nmCliente,
        Integer cdAtendente,
        String nmAtendente,
        java.time.LocalDateTime dataVenda, // ✅ MUDOU: LocalDateTime → LocalDate
        Double vlTotal,
        Double desconto,
        FormaPagamento formaPagamento
) {}