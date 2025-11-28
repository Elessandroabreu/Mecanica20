package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.TipoOrdemOrcamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para Ordem de Serviço e Orçamentos
 *
 * IMPORTANTE: O Model OrdemServico NÃO TEM o campo "observacoes"
 * Observações ficam no AgendamentoModel relacionado
 */
public record OrdemServicoDTO(
        @NotNull(message = "Cliente é obrigatório")
        Integer cdCliente,

        @NotNull(message = "Veículo é obrigatório")
        Integer cdVeiculo,

        @NotNull(message = "Mecânico é obrigatório")
        Integer cdMecanico,

        @NotNull(message = "Tipo de serviço é obrigatório")
        TipoOrdemOrcamento tipoServico,

        LocalDate dataAgendamento,

        @PositiveOrZero(message = "Valor da mão de obra deve ser zero ou positivo")
        Double vlMaoObra,

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