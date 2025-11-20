package com.oficinamecanica.OficinaMecanica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    private Integer cdCliente;
    private String nmCliente;
    private String nuCPF;
    private String nuTelefone;
    private String dsEndereco;
    private String email;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
}
