package com.oficinamecanica.OficinaMecanica.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.enums.TipoOrdemOrcamento;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Column(name = "TIPOORDEMORCAMENTO", nullable = false, length = 20)
    private TipoOrdemOrcamento tipoOrdemOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private Status status = Status.AGENDADO;

    @Column(name = "DATAAGENDAMENTO", nullable = false)
    private LocalDateTime dataAgendamento;

    // SOMENTE PRODUTOS
    @Column(name = "VLPECAS")
    private Double vlPecas = 0.0;

    // SOMENTE SERVIÇOS PADRÃO (classe Servico)
    @Column(name = "VLSERVICOS")
    private Double vlServicos = 0.0;

    // MÃO DE OBRA AVULSA DIGITADA MANUALMENTE
    @Column(name = "VLMAOOBRAEXTRA")
    private Double vlMaoObraExtra = 0.0;

    // SOMA TOTAL
    @Column(name = "VLTOTAL")
    private Double vlTotal = 0.0;

    @Column(name = "DIAGNOSTICO", length = 1000)
    private String diagnostico;

    @Column(name = "APROVADO")
    private Boolean aprovado = false;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Agendamento agendamento;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<ItemOrdemServico> itens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dataAbertura = LocalDateTime.now();
    }
}