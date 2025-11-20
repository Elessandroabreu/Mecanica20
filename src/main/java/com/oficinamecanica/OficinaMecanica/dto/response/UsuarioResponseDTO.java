package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.models.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Integer cdUsuario;
    private String nmUsuario;
    private String email;
    private Usuario.AuthProvider provider;
    private Set<Usuario.UserRole> roles;
    private String nuTelefone;
    private String nuCPF;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
