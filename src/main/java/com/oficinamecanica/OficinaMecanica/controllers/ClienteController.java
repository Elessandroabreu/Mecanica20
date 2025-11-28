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

    // ✅ CORRIGIDO: Nome da variável agora bate com o DTO
    @Column(name = "CPF", nullable = false, length = 14, unique = true)
    private String nuCPF;

    // ✅ CORRIGIDO: Nome da variável agora bate com o DTO
    @Column(name = "TELEFONE", nullable = false, length = 20)
    private String nuTelefone;

    // ✅ CORRIGIDO: Nome da variável agora bate com o DTO
    @Column(name = "ENDERECO", length = 255)
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