package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import com.oficinamecanica.OficinaMecanica.models.Venda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendaResponseDTO {
    private Integer cdVenda;
    private Integer cdCliente;
    private String nmCliente;
    private Integer cdAtendente;
    private String nmAtendente;
    private LocalDateTime dataVenda;
    private Double vlTotal;
    private Double desconto;
    private FormaPagamento formaPagamento;
    private LocalDateTime dataCadastro;
}
