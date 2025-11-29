package com.oficinamecanica.OficinaMecanica.models;

import com.oficinamecanica.OficinaMecanica.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate; // ✅ MUDOU: Era LocalDateTime

@Entity
@Table(name = "AGENDAMENTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDAGENDAMENTO")
    private Integer cdAgendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDCLIENTE", nullable = false)
    private ClienteModel cdCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDVEICULO", nullable = false)
    private VeiculoModel veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDMECANICO", nullable = false)
    private UsuarioModel mecanico;

    @Column(name = "DATAAGENDAMENTO", nullable = false)
    private LocalDate dataAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private Status status = Status.AGENDADO;

    @Column(name = "OBSERVACOES", length = 1000)
    private String observacoes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDORDEMSERVICO")
    private OrdemServicoModel ordemServico;
}