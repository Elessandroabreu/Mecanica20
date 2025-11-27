// OrdemServicoResponseDTO.java
package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.StatusOrdemServico;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponseDTO(
        Integer cdOrdemServico,
        Integer cdCliente,
        String nmCliente, // ✅ Nome do cliente
        Integer cdVeiculo,
        String placa, // ✅ Placa do veículo
        String modeloVeiculo, // ✅ NOVO: Modelo do veículo
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
        Boolean aprovado,
        List<ItemOrdemServicoResponseDTO> itens
) {}