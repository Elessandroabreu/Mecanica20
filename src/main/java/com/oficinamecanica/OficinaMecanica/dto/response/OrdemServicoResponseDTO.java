package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.StatusOrdemServico;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
import java.time.LocalDateTime;

public record OrdemServicoResponseDTO(
        Integer cdOrdemServico,
        Integer cdCliente,
        String nmCliente,
        Integer cdVeiculo,
        String placa,
        Integer cdMecanico,
        String nmMecanico,
        TipoServico tipoServico,
        StatusOrdemServico statusOrdemServico,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Double vlPecas,
        Double vlMaoObra,
        Double vlTotal,
        Double desconto,
        String observacoes,
        String diagnostico,
        Boolean aprovado
) {}