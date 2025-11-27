package com.oficinamecanica.OficinaMecanica.models;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate; // ✅ MUDOU: Era LocalDateTime

@Entity
@Table(name = "AGENDAMENTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDAGENDAMENTO")
    private Integer cdAgendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDCLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDVEICULO", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDMECANICO", nullable = false)
    private Usuario mecanico;

    @Column(name = "DATAAGENDAMENTO", nullable = false)
    private LocalDate dataAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(name = "OBSERVACOES", length = 1000)
    private String observacoes;
}