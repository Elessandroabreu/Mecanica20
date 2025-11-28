package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.FaturamentoDTO;
import com.oficinamecanica.OficinaMecanica.models.Faturamento;
import com.oficinamecanica.OficinaMecanica.repositories.FaturamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de Faturamento
 * Gerencia consultas e relatórios de faturamento
 *
 * OBS: O faturamento é criado automaticamente quando:
 * - Uma venda é concluída (VendaService)
 * - Uma ordem de serviço é concluída (OrdemServicoService)
 */
@Service
@RequiredArgsConstructor
public class FaturamentoService {

    private final FaturamentoRepository faturamentoRepository;

    /**
     * BUSCAR FATURAMENTO POR ID
     */
    @Transactional(readOnly = true)
    public FaturamentoDTO buscarPorId(Integer id) {
        Faturamento faturamento = faturamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Faturamento não encontrado"));
        return converterParaDTO(faturamento);
    }

    /**
     * LISTAR FATURAMENTO POR PERÍODO
     * Exemplo: buscar faturamento de 01/01/2024 até 31/01/2024
     */
    @Transactional(readOnly = true)
    public List<FaturamentoDTO> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return faturamentoRepository.findFaturamentosNoPeriodo(dataInicio, dataFim).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * CALCULAR TOTAL FATURADO NO PERÍODO
     * Retorna a soma de todos os valores faturados entre duas datas
     */
    @Transactional(readOnly = true)
    public Double calcularTotalPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        Double total = faturamentoRepository.calcularTotalFaturadoNoPeriodo(dataInicio, dataFim);

        // Se não houver faturamento, retorna 0.0
        return total != null ? total : 0.0;
    }

    /**
     * LISTAR FATURAMENTO DO DIA ATUAL
     */
    @Transactional(readOnly = true)
    public List<FaturamentoDTO> listarFaturamentoDoDia() {
        return faturamentoRepository.findFaturamentosDoDia(LocalDateTime.now()).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * CALCULAR TOTAL FATURADO NO DIA ATUAL
     * Útil para relatórios de caixa diário
     */
    @Transactional(readOnly = true)
    public Double calcularTotalDoDia() {
        Double total = faturamentoRepository.calcularTotalFaturadoDoDia(LocalDateTime.now());
        return total != null ? total : 0.0;
    }

    // ========== MÉTODO AUXILIAR ==========

    /**
     * CONVERTER MODEL PARA DTO
     */
    private FaturamentoDTO converterParaDTO(Faturamento faturamento) {
        return new FaturamentoDTO(
                faturamento.getCdFaturamento(),
                // Se foi venda, pega o ID da venda
                faturamento.getVenda() != null ? faturamento.getVenda().getCdVenda() : null,
                // Se foi ordem de serviço, pega o ID da OS
                faturamento.getOrdemServico() != null ? faturamento.getOrdemServico().getCdOrdemServico() : null,
                faturamento.getDataVenda(),
                faturamento.getVlTotal(),
                faturamento.getFormaPagamento()
        );
    }
}