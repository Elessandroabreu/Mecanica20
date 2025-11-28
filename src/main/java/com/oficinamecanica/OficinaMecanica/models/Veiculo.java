package com.oficinamecanica.OficinaMecanica.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "VEICULO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDVEICULO")
    private Integer cdVeiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CDCLIENTE", nullable = false)
    private ClienteModel clienteModel;

    @Column(name = "PLACA", nullable = false, length = 10, unique = true)
    private String placa;

    @Column(name = "MODELO", nullable = false, length = 100)
    private String modelo;

    @Column(name = "MARCA", nullable = false, length = 50)
    private String marca;

    @Column(name = "ANO", nullable = false)
    private Integer ano;

    @Column(name = "COR", length = 30)
    private String cor;

    @OneToMany(mappedBy = "veiculo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrdemServico> ordensServico;
}
