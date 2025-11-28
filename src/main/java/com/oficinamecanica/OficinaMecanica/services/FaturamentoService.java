package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.models.Faturamento;
import com.oficinamecanica.OficinaMecanica.repositories.FaturamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaturamentoService {

    private final FaturamentoRepository faturamentoRepository;

    @Transactional(readOnly = true)
    public FaturamentoResponseDTO buscarPorId(Integer id) {
        Faturamento faturamento = faturamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Faturamento não encontrado"));
        return converterParaDTO(faturamento);
    }

    @Transactional(readOnly = true)
    public List<FaturamentoResponseDTO> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return faturamentoRepository.findFaturamentosNoPeriodo(dataInicio, dataFim).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calcularTotalPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        Double total = faturamentoRepository.calcularTotalFaturadoNoPeriodo(dataInicio, dataFim);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public List<FaturamentoResponseDTO> listarFaturamentoDoDia() {
        return faturamentoRepository.findFaturamentosDoDia(LocalDateTime.now()).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calcularTotalDoDia() {
        Double total = faturamentoRepository.calcularTotalFaturadoDoDia(LocalDateTime.now());
        return total != null ? total : 0.0;
    }

    private FaturamentoResponseDTO converterParaDTO(Faturamento faturamento) {
        String nomeCliente = null;
        String tipoTransacao = null;

        if (faturamento.getVenda() != null) {
            nomeCliente = faturamento.getVenda().getClienteModel().getNmCliente();
            tipoTransacao = "VENDA";
        } else if (faturamento.getOrdemServico() != null) {
            nomeCliente = faturamento.getOrdemServico().getClienteModel().getNmCliente();
            tipoTransacao = "SERVICO";
        }

        return new FaturamentoResponseDTO(
                faturamento.getCdFaturamento(),
                faturamento.getVenda() != null ? faturamento.getVenda().getCdVenda() : null,
                faturamento.getOrdemServico() != null ? faturamento.getOrdemServico().getCdOrdemServico() : null,
                faturamento.getDataVenda(),
                faturamento.getVlTotal(),
                faturamento.getFormaPagamento(),
                nomeCliente,
                tipoTransacao
        );
    }
}