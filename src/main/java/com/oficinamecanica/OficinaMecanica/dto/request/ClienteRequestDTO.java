package com.oficinamecanica.OficinaMecanica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDTO {

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nmCliente;

    @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
    private String nuCPF;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String nuTelefone;

    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String dsEndereco;

    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;
}
