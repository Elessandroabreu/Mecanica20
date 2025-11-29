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

import java.time.LocalDate;
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
        return ResponseEntity.ok(faturamentoService.buscarPorId(id));
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar faturamento por período")
    public ResponseEntity<List<FaturamentoDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        return ResponseEntity.ok(
                faturamentoService.listarPorPeriodo(dataInicio, dataFim)
        );
    }

    @GetMapping("/total-periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Calcular total faturado no período")
    public ResponseEntity<Map<String, Double>> calcularTotalPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        Double total = faturamentoService.calcularTotalPeriodo(dataInicio.atStartOfDay(), dataFim.atStartOfDay());
        return ResponseEntity.ok(Map.of("totalFaturado", total));
    }

    @GetMapping("/dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar faturamento do dia")
    public ResponseEntity<List<FaturamentoDTO>> listarDoDia() {
        return ResponseEntity.ok(faturamentoService.listarFaturamentoDoDia());
    }

    @GetMapping("/total-dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Calcular total faturado no dia")
    public ResponseEntity<Map<String, Double>> calcularTotalDia() {
        Double total = faturamentoService.calcularTotalDoDia();
        return ResponseEntity.ok(Map.of("totalDia", total));
    }
}
