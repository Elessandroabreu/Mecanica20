package com.oficinamecanica.OficinaMecanica.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "USUARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDUSUARIO")
    private Integer cdUsuario;

    @Column(name = "NMUSUARIO", nullable = false, length = 120)
    private String nmUsuario;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROVIDER", nullable = false, length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "USUARIO_ROLE",
        joinColumns = @JoinColumn(name = "CDUSUARIO")
    )
    @Column(name = "ROLE", length = 20)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    @Column(name = "NUTELEFONE", length = 20)
    private String nuTelefone;

    @Column(name = "NUCPF", length = 14, unique = true)
    private String nuCPF;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    @Column(name = "DATACADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "DATAATUALIZACAO")
    private LocalDateTime dataAtualizacao;

    @Column(name = "PROVIDERID", unique = true, length = 255)
    private String providerId;

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }

    public enum AuthProvider {
        LOCAL,
        GOOGLE
    }

    public enum UserRole {
        ROLE_ADMIN,
        ROLE_ATENDENTE,
        ROLE_MECANICO
    }
}
