package com.oficinamecanica.OficinaMecanica.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oficinamecanica.OficinaMecanica.enums.StatusOrdemServico;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
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

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Agendamento agendamento;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ItemOrdemServico> itens;

    @PrePersist
    protected void onCreate() {
        this.dataAbertura = LocalDateTime.now();
    }
}