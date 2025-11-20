package com.oficinamecanica.OficinaMecanica.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoRequestDTO {

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nmProduto;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String dsProduto;

    @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
    private String categoria;

    @NotNull(message = "Valor de custo é obrigatório")
    @Positive(message = "Valor de custo deve ser positivo")
    private Double vlCusto;

    @NotNull(message = "Valor de venda é obrigatório")
    @Positive(message = "Valor de venda deve ser positivo")
    private Double vlVenda;

    @NotNull(message = "Quantidade em estoque é obrigatória")
    @PositiveOrZero(message = "Quantidade em estoque deve ser zero ou positiva")
    private Integer qtdEstoque;

    @NotNull(message = "Quantidade mínima é obrigatória")
    @PositiveOrZero(message = "Quantidade mínima deve ser zero ou positiva")
    private Integer qtdMinimo;

    @AssertTrue(message = "Preço de venda deve ser maior que o custo")
    public boolean isPrecoVendaMaiorQueCusto() {
        if (vlVenda == null || vlCusto == null) return true;
        return vlVenda > vlCusto;
    }
}
