package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record VendaRequestDTO(
        @NotNull(message = "Cliente é obrigatório")
        Integer cdCliente,

        @NotNull(message = "Atendente é obrigatório")
        Integer cdAtendente,

        @PositiveOrZero(message = "Desconto deve ser zero ou positivo")
        Double desconto,

        @NotNull(message = "Forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,

        @NotNull(message = "Itens são obrigatórios")
        List<ItemVendaDTO> itens
) {
    public record ItemVendaDTO(
            @NotNull(message = "Produto é obrigatório")
            Integer cdProduto,

            @NotNull(message = "Quantidade é obrigatória")
            @Positive(message = "Quantidade deve ser positiva")
            Integer quantidade
    ) {}
}