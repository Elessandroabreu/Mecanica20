package com.oficinamecanica.OficinaMecanica.models;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "USUARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CDUSUARIO")
    private Integer cdUsuario;

    @Column(name = "NMUSUARIO", nullable = false, length = 120)
    private String nmUsuario;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "SENHA", length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROVIDER", nullable = false, length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "USUARIO_ROLE", joinColumns = @JoinColumn(name = "CDUSUARIO")
    )
    @Column(name = "ROLE", length = 20)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @Column(name = "CPF", length = 14, unique = true)
    private String cpf;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    @Column(name = "PROVIDERID", unique = true, length = 255)
    private String providerId;
}