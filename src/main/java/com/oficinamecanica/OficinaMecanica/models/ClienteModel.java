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
public class ClienteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDCLIENTE")
    private Integer cdCliente;

    @Column(name = "NMCLIENTE", nullable = false, length = 120)
    private String nmCliente;

    @Column(name = "CPF", nullable = false, length = 14, unique = true)
    private String CPF;

    @Column(name = "TELEFONE", nullable = false,length = 20)
    private String Telefone;

    @Column(name = "ENDERECO", length = 255)
    private String Endereco;

    @Column(name = "EMAIL", length = 150)
    private String email;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "clienteModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Veiculo> veiculos;

    // ✅ CORRIGIDO: "cliente" → "clienteModel"
    @OneToMany(mappedBy = "clienteModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrdemServico> ordensServico;

    // ✅ CORRIGIDO: "cliente" → "clienteModel"
    @OneToMany(mappedBy = "clienteModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Venda> vendas;
}