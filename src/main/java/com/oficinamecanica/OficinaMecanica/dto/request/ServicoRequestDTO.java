package com.oficinamecanica.OficinaMecanica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicoRequestDTO {

    @NotBlank(message = "Nome do serviço é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nmServico;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String dsServico;

    @NotNull(message = "Valor do serviço é obrigatório")
    @Positive(message = "Valor do serviço deve ser positivo")
    private Double vlServico;

    @NotNull(message = "Tempo estimado é obrigatório")
    @Positive(message = "Tempo estimado deve ser positivo")
    private Integer tmpEstimado;
}
