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
public class ServicoResponseDTO {
    private Integer cdServico;
    private String nmServico;
    private String dsServico;
    private Double vlServico;
    private Integer tmpEstimado;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
}
