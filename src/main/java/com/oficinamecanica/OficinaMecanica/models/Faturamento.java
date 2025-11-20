package com.oficinamecanica.OficinaMecanica.models;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FATURAMENTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faturamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDFATURAMENTO")
    private Integer cdFaturamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDVENDA")
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDORDEMSERVICO")
    private OrdemServico ordemServico;

    @Column(name = "DATAVENDA", nullable = false)
    private LocalDateTime dataVenda;

    @Column(name = "VLTOTAL", nullable = false)
    private Double vlTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "FORMAPAGAMENTO", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "DATACADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();

    }
  }
