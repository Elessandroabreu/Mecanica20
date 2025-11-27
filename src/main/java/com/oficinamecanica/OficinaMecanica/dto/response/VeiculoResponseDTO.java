package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.models.Veiculo;

public record VeiculoResponseDTO(
        Integer cdVeiculo,
        Integer cdCliente,
        String nmCliente,
        String placa,
        String modelo,
        String marca,
        Integer ano,
        String cor
) {
    public VeiculoResponseDTO(Veiculo veiculo) {
        this(
                veiculo.getCdVeiculo(),
                veiculo.getCliente() != null ? veiculo.getCliente().getCdCliente() : null,
                veiculo.getCliente() != null ? veiculo.getCliente().getNmCliente() : null,
                veiculo.getPlaca(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getAno(),
                veiculo.getCor()
        );
    }
}