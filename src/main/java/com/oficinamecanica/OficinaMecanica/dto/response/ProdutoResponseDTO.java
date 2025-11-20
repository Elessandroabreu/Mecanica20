package com.oficinamecanica.OficinaMecanica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponseDTO {
    private Integer cdProduto;
    private String nmProduto;
    private String dsProduto;
    private String categoria;
    private Double vlCusto;
    private Double vlVenda;
    private Integer qtdEstoque;
    private Integer qtdMinimo;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
}
