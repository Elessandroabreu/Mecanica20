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

    @Column(name = "CATEGORIA", length = 100)
    private String categoria;

    @Column(name = "VLCUSTO", nullable = false)
    private Double vlCusto;

    @Column(name = "VLVENDA", nullable = false)
    private Double vlVenda;

    @Column(name = "QTDESTOQUE", nullable = false)
    private Integer qtdEstoque = 0;

    @Column(name = "QTDMINIMO", nullable = false)
    private Integer qtdMinimo = 5;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;
}