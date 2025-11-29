package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.enums.TipoOrdemOrcamento;

import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponseDTO(
        Integer cdOrdemServico,

        Integer cdCliente,
        String nomeCliente,

        Integer cdVeiculo,
        String placaVeiculo,
        String modeloVeiculo,
        String marcaVeiculo,

        Integer cdMecanico,
        String nomeMecanico,

        TipoOrdemOrcamento tipoOrdemOrcamento,
        Status status,

        LocalDateTime dataAgendamento,
        LocalDateTime dataAbertura,

        Double vlPecas,
        Double vlServicos,
        Double vlMaoObraExtra,
        Double vlTotal,

        String diagnostico,
        Boolean aprovado,

        List<ItemResponseDTO> itens
) {

    public record ItemResponseDTO(
            Integer cdItem,
            Integer cdProduto,
            String nomeProduto,
            Integer cdServico,
            String nomeServico,
            Integer quantidade,
            Double vlUnitario,
            Double vlTotal
    ) {}
}
