package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrdemServicoRequestDTO(
        @NotNull(message = "Cliente é obrigatório")
        Integer cdCliente,

        @NotNull(message = "Veículo é obrigatório")
        Integer cdVeiculo,

        @NotNull(message = "Mecânico é obrigatório")
        Integer cdMecanico,

        @NotNull(message = "Tipo de serviço é obrigatório")
        TipoServico tipoServico,

        @PositiveOrZero(message = "Valor da mão de obra deve ser zero ou positivo")
        Double vlMaoObra,

        @PositiveOrZero(message = "Desconto deve ser zero ou positivo")
        Double desconto,

        @Size(max = 1000, message = "Observações deve ter no máximo 1000 caracteres")
        String observacoes,

        @Size(max = 1000, message = "Diagnóstico deve ter no máximo 1000 caracteres")
        String diagnostico,

        List<ItemDTO> itens
) {
    public record ItemDTO(
            Integer cdProduto,
            Integer cdServico,

            @NotNull(message = "Quantidade é obrigatória")
            @PositiveOrZero(message = "Quantidade deve ser zero ou positiva")
            Integer quantidade
    ) {}
}