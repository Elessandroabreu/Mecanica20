package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.models.OrdemServico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoResponseDTO {
    private Integer cdOrdemServico;
    private Integer cdCliente;
    private String nmCliente;
    private Integer cdVeiculo;
    private String placa;
    private Integer cdMecanico;
    private String nmMecanico;
    private OrdemServico.TipoServico tipoServico;
    private OrdemServico.StatusOrdemServico statusOrdemServico;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private Double vlPecas;
    private Double vlMaoObra;
    private Double vlTotal;
    private Double desconto;
    private String observacoes;
    private String diagnostico;
    private Boolean aprovado;
}
