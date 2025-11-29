package com.oficinamecanica.OficinaMecanica.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.enums.TipoOrdemOrcamento;

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
public class OrdemServicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDORDEMSERVICO")
    private Integer cdOrdemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDCLIENTE", nullable = false)
    private ClienteModel clienteModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDVEICULO", nullable = false)
    private VeiculoModel veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDMECANICO", nullable = false)
    private UsuarioModel mecanico;

    // ORDEM_SERVICO ou ORCAMENTO
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPOORDEMORCAMENTO", nullable = false, length = 20)
    private TipoOrdemOrcamento tipoOrdemOrcamento;

    // Status será definido automaticamente no @PrePersist
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private Status status;

    // Para orçamento não é obrigatório
    @Column(name = "DATAAGENDAMENTO")
    private LocalDateTime dataAgendamento;

    @Column(name = "VLPECAS")
    private Double vlPecas = 0.0;

    @Column(name = "VLSERVICOS")
    private Double vlServicos = 0.0;

    @Column(name = "VLMAOOBRAEXTRA")
    private Double vlMaoObraExtra = 0.0;

    @Column(name = "VLTOTAL")
    private Double vlTotal = 0.0;

    @Column(name = "DIAGNOSTICO", length = 1000)
    private String diagnostico;

    @Column(name = "APROVADO")
    private Boolean aprovado = false;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private AgendamentoModel agendamentoModel;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<ItemOrdemServicoModel> itens = new ArrayList<>();

    @Column(name = "DATAABERTURA", nullable = false)
    private LocalDateTime dataAbertura;

    // ================================
    // DEFINIR STATUS AUTOMATICAMENTE
    // ================================
    @PrePersist
    protected void onCreate() {
        this.dataAbertura = LocalDateTime.now();

        if (this.tipoOrdemOrcamento == TipoOrdemOrcamento.ORCAMENTO) {
            this.status = Status.ORCAMENTO;
        } else {
            this.status = Status.AGENDADO;
        }
    }
}
