package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaturamentoResponseDTO {
    private Integer cdFaturamento;
    private Integer cdVenda;
    private Integer cdOrdemServico;
    private LocalDateTime dataVenda;
    private Double vlTotal;
    private FormaPagamento formaPagamento;
    private String nomeCliente;
    private String tipoTransacao; // "VENDA" ou "SERVICO"
}