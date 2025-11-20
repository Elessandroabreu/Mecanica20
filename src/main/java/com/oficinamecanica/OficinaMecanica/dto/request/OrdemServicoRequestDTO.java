package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.models.OrdemServico;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoRequestDTO {

    @NotNull(message = "Cliente é obrigatório")
    private Integer cdCliente;

    @NotNull(message = "Veículo é obrigatório")
    private Integer cdVeiculo;

    @NotNull(message = "Mecânico é obrigatório")
    private Integer cdMecanico;

    @NotNull(message = "Tipo de serviço é obrigatório")
    private OrdemServico.TipoServico tipoServico;

    @PositiveOrZero(message = "Valor da mão de obra deve ser zero ou positivo")
    private Double vlMaoObra;

    @PositiveOrZero(message = "Desconto deve ser zero ou positivo")
    private Double desconto;

    @Size(max = 1000, message = "Observações deve ter no máximo 1000 caracteres")
    private String observacoes;

    @Size(max = 1000, message = "Diagnóstico deve ter no máximo 1000 caracteres")
    private String diagnostico;

    private List<ItemDTO> itens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {
        private Integer cdProduto;
        private Integer cdServico;

        @NotNull(message = "Quantidade é obrigatória")
        @PositiveOrZero(message = "Quantidade deve ser zero ou positiva")
        private Integer quantidade;
    }
}
