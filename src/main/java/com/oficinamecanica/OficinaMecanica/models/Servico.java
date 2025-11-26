package com.oficinamecanica.OficinaMecanica.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SERVICO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDSERVICO")
    private Integer cdServico;

    @Column(name = "NMSERVICO", nullable = false, length = 150)
    private String nmServico;

    @Column(name = "DSSERVICO", length = 500)
    private String dsServico;

    @Column(name = "VLSERVICO", nullable = false)
    private Double vlServico;

    @Column(name = "TMPESTIMADO", nullable = false)
    private Integer tmpEstimado;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;
}