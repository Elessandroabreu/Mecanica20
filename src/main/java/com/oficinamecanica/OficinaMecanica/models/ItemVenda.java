package com.oficinamecanica.OficinaMecanica.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ITEMVENDA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDITEMVENDA")
    private Integer cdItemVenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDVENDA", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDPRODUTO", nullable = false)
    private Produto produto;

    @Column(name = "QUANTIDADE", nullable = false)
    private Integer quantidade;

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
