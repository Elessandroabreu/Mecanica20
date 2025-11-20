package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

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


    @NotBlank(message = "Senha é obrigatória", groups = LocalAuth.class)
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;


    public interface LocalAuth {}

    @NotNull(message = "Provider é obrigatório")
    private AuthProvider provider;

    @NotNull(message = "Perfis são obrigatórios")
    private Set<UserRole> roles;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String nuTelefone;

    @CPF
    @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
    private String nuCPF;

    private String providerId;

    private Boolean ativo = true;
}
