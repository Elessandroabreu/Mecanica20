package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.OficinaMecanica.models.Venda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendaRequestDTO {

    @NotNull(message = "Cliente é obrigatório")
    private Integer cdCliente;

    @NotNull(message = "Atendente é obrigatório")
    private Integer cdAtendente;

    @PositiveOrZero(message = "Desconto deve ser zero ou positivo")
    private Double desconto;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private Venda.FormaPagamento formaPagamento;

    @NotNull(message = "Itens são obrigatórios")
    private List<ItemVendaDTO> itens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemVendaDTO {
        @NotNull(message = "Produto é obrigatório")
        private Integer cdProduto;

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser positiva")
        private Integer quantidade;
    }
}
