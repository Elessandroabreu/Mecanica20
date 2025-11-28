package com.oficinamecanica.OficinaMecanica.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDPRODUTO")
    private Integer cdProduto;

    @Column(name = "NMPRODUTO", nullable = false, length = 150)
    private String nmProduto;

    @Column(name = "DSPRODUTO", length = 500)
    private String dsProduto;

    @Column(name = "CATEGORIA", nullable = false, length = 100)
    private String categoria;

    @Column(name = "VLPRODUTO", nullable = false)
    private Double vlProduto;

    @Column(name = "QTDESTOQUE")
    private Integer qtdEstoque = 0;

    @Column(name = "QTDMINIMOESTOQUE", nullable = false)
    private Integer qtdMinimoEstoque = 5;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;
}