package com.oficinamecanica.OficinaMecanica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteDTO(
        Integer cdCliente, // ✅ ADICIONADO - para retorno

        @NotBlank(message = "Nome do cliente é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nmCliente,

        @NotBlank(message = "CPF obrigatório")
        @CPF(message = "CPF inválido")
        @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
        String CPF, // ✅ CORRIGIDO de "CPF" para "nuCPF"

        @NotBlank(message = "Telefone obrigatório")
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String Telefone, // ✅ CORRIGIDO de "telefone" para "nuTelefone"

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
        String Endereco, // ✅ CORRIGIDO de "endereco" para "dsEndereco"

        @NotBlank(message = "E-mail obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email,

        Boolean ativo // ✅ ADICIONADO - para retorno
) {}