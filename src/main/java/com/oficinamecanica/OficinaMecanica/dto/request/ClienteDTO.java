package com.oficinamecanica.OficinaMecanica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteDTO(
        @NotBlank(message = "Nome do cliente é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nmCliente,

        @NotBlank(message = "CPF obrigatório")
        @CPF(message = "CPF inválido")
        @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
        String CPF,

        @NotBlank
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
        String endereco,

        @NotBlank(message = "E-mail obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email
) {}