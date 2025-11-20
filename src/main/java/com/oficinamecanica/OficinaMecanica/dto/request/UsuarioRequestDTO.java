package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.models.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nmUsuario;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotNull(message = "Provider é obrigatório")
    private Usuario.AuthProvider provider;

    @NotNull(message = "Perfis são obrigatórios")
    private Set<Usuario.UserRole> roles;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String nuTelefone;

    @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
    private String nuCPF;

    private String providerId;

    private Boolean ativo = true;
}
