package com.oficinamecanica.OficinaMecanica.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "CLIENTE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDCLIENTE")
    private Integer cdCliente;

    @Column(name = "NMCLIENTE", nullable = false, length = 120)
    private String nmCliente;

    @Column(name = "NUCPF", length = 14, unique = true)
    private String nuCPF;

    @Column(name = "NUTELEFONE", length = 20)
    private String nuTelefone;

    @Column(name = "DSENDERECO", length = 255)
    private String dsEndereco;

    @Column(name = "EMAIL", length = 150)
    private String email;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Veiculo> veiculos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrdemServico> ordensServico;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Venda> vendas;
}