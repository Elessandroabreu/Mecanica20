package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.FaturamentoDTO;
import com.oficinamecanica.OficinaMecanica.models.FaturamentoModel;
import com.oficinamecanica.OficinaMecanica.repositories.FaturamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaturamentoService {

    private final FaturamentoRepository faturamentoRepository;

    @Transactional(readOnly = true)
    public FaturamentoDTO buscarPorId(Integer id) {
        FaturamentoModel faturamento = faturamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Faturamento não encontrado"));
        return converterParaDTO(faturamento);
    }

    @Transactional(readOnly = true)
    public List<FaturamentoDTO> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
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
    public List<FaturamentoDTO> listarFaturamentoDoDia() {
        return faturamentoRepository.findFaturamentosDoDia(LocalDateTime.now()).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calcularTotalDoDia() {
        Double total = faturamentoRepository.calcularTotalFaturadoDoDia(LocalDateTime.now());
        return total != null ? total : 0.0;
    }

    public List<FaturamentoDTO> listarPorPeriodo(LocalDate inicio, LocalDate fim) {

        List<FaturamentoModel> lista = faturamentoRepository
                .findByDataVendaBetween(inicio.atStartOfDay(), fim.atTime(23, 59, 59));

        return lista.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private FaturamentoDTO converterParaDTO(FaturamentoModel faturamento) {
        return new FaturamentoDTO(
                faturamento.getCdFaturamento(),
                faturamento.getVenda() != null ? faturamento.getVenda().getCdVenda() : null,
                faturamento.getOrdemServico() != null ? faturamento.getOrdemServico().getCdOrdemServico() : null,
                faturamento.getDataVenda().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                faturamento.getVlTotal(),
                faturamento.getFormaPagamento()
        );
    }
}