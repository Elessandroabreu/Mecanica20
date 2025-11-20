package com.oficinamecanica.OficinaMecanica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoResponseDTO {
    private Integer cdVeiculo;
    private Integer cdCliente;
    private String nmCliente;
    private String placa;
    private String modelo;
    private String marca;
    private Integer ano;
    private String cor;
    private LocalDateTime dataCadastro;
}
