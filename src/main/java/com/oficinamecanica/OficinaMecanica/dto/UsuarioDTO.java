package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;
import java.util.Set;

public record UsuarioDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nmUsuario,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email,

        @Size(min = 3, message = "Senha deve ter no mínimo 3 caracteres")
        String senha,

        @NotNull(message = "Provider é obrigatório")
        AuthProvider provider,

        @NotNull(message = "Perfis são obrigatórios")
        Set<UserRole> roles,

        @Size(max = 15, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotBlank(message = "CPF obrigatório")
        @CPF(message = "CPF inválido")
        @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
        String cpf,

        String providerId,

        Boolean ativo
) {
    public UsuarioDTO {
        if (ativo == null) {
            ativo = true;
        }
    }
}