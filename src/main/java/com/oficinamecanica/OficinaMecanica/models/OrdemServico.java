package com.oficinamecanica.OficinaMecanica.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ORDEMSERVICO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDORDEMSERVICO")
    private Integer cdOrdemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDCLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDVEICULO", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDMECANICO", nullable = false)
    private Usuario mecanico;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPOSERVICO", nullable = false, length = 20)
    private TipoServico tipoServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUSORDEMSERVICO", nullable = false, length = 20)
    private StatusOrdemServico statusOrdemServico = StatusOrdemServico.AGUARDANDO;

    @Column(name = "DATAABERTURA", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "DATAFECHAMENTO")
    private LocalDateTime dataFechamento;

    @Column(name = "VLPECAS")
    private Double vlPecas = 0.0;

    @Column(name = "VLMAOOBRA")
    private Double vlMaoObra = 0.0;

    @Column(name = "VLTOTAL")
    private Double vlTotal = 0.0;

    @Column(name = "DESCONTO")
    private Double desconto = 0.0;

    @Column(name = "OBSERVACOES", length = 1000)
    private String observacoes;

    @Column(name = "DIAGNOSTICO", length = 1000)
    private String diagnostico;

    @Column(name = "APROVADO")
    private Boolean aprovado = false;

    @Column(name = "DATACADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemOrdemServico> itens;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
        this.dataAbertura = LocalDateTime.now();
    }

    public enum TipoServico {
        ORCAMENTO,
        ORDEM_DE_SERVICO
    }

    public enum StatusOrdemServico {
        AGUARDANDO,
        EM_ANDAMENTO,
        CONCLUIDA,
        CANCELADA
    }
}
