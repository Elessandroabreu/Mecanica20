package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.FaturamentoDTO;
import com.oficinamecanica.OficinaMecanica.services.FaturamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faturamento")
@RequiredArgsConstructor
@Tag(name = "Faturamento", description = "Endpoints para gerenciamento de faturamento")
public class FaturamentoController {


    private final FaturamentoService faturamentoService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Buscar faturamento por ID")
    public ResponseEntity<FaturamentoDTO> buscarPorId(@PathVariable Integer id) {
        FaturamentoDTO response = faturamentoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar faturamento por período")
    public ResponseEntity<List<FaturamentoDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<FaturamentoDTO> response = faturamentoService.listarPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Calcular total faturado no período")
    public ResponseEntity<Map<String, Double>> calcularTotalPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        Double total = faturamentoService.calcularTotalPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(Map.of("totalFaturado", total));
    }

    @GetMapping("/dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar faturamento do dia")
    public ResponseEntity<List<FaturamentoDTO>> listarDoDia() {
        List<FaturamentoDTO> response = faturamentoService.listarFaturamentoDoDia();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Calcular total faturado no dia")
    public ResponseEntity<Map<String, Double>> calcularTotalDia() {
        Double total = faturamentoService.calcularTotalDoDia();
        return ResponseEntity.ok(Map.of("totalDia", total));
    }
}