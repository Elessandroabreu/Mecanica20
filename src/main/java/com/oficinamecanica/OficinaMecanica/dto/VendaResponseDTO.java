package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponseDTO(
        Integer cdVenda,
        LocalDateTime dataVenda,
        Double vlTotal,
        Double desconto,
        FormaPagamento formaPagamento,

        ClienteBasicDTO clienteModel,
        AtendenteBasicDTO atendente,
        List<ItemVendaResponseDTO> itens
) {
    public record ClienteBasicDTO(
            Integer cdCliente,
            String nmCliente,
            String nuCPF,
            String nuTelefone,
            String email
    ) {}

    public record AtendenteBasicDTO(
            Integer cdUsuario,
            String nmUsuario,
            String email
    ) {}

    public record ItemVendaResponseDTO(
            Integer cdItemVenda,
            Integer cdProduto,
            String nmProduto,
            Integer quantidade,
            Double vlUnitario,
            Double vlTotal
    ) {}
}