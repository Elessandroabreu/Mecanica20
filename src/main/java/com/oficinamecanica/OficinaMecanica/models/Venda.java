package com.oficinamecanica.OficinaMecanica.models;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "VENDA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDVENDA")
    private Integer cdVenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDCLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDATENDENTE", nullable = false)
    private Usuario atendente;

    @Column(name = "DATAVENDA", nullable = false)
    private LocalDateTime dataVenda;

    @Column(name = "VLTOTAL", nullable = false)
    private Double vlTotal = 0.0;

    @Column(name = "DESCONTO")
    private Double desconto = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "FORMAPAGAMENTO", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "DATACADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemVenda> itens;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
        this.dataVenda = LocalDateTime.now();
    }

    }
