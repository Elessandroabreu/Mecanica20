package com.oficinamecanica.OficinaMecanica.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ITEMORDEMSERVICO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOrdemServicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDITEMORDEMSERVICO")
    private Integer cdItemOrdemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDORDEMSERVICO", nullable = false)
    private OrdemServicoModel ordemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDPRODUTO")
    private ProdutoModel produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDSERVICO")
    private ServicoModel servico;

    @Column(name = "QUANTIDADE", nullable = false)
    private Integer quantidade = 1;

    @Column(name = "VLUNITARIO", nullable = false)
    private Double vlUnitario;

    @Column(name = "VLTOTAL", nullable = false)
    private Double vlTotal;

    @PrePersist
    @PreUpdate
    protected void calcularTotal() {
        if (this.vlUnitario != null && this.quantidade != null) {
            this.vlTotal = this.vlUnitario * this.quantidade;
        }
    }
}
