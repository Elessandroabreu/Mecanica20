package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.VendaDTO;
import com.oficinamecanica.OficinaMecanica.services.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "Endpoints para gerenciamento de vendas no balcão")
public class VendaController {

    private final VendaService vendaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Realizar nova venda no balcão")
    public ResponseEntity<VendaDTO> criar(@Valid @RequestBody VendaDTO dto) {
        VendaDTO response = vendaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Buscar venda por ID")
    public ResponseEntity<VendaDTO> buscarPorId(@PathVariable Integer id) {
        VendaDTO response = vendaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar todas as vendas")
    public ResponseEntity<List<VendaDTO>> listarTodas() {
        List<VendaDTO> response = vendaService.listarTodas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cliente/{cdCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar vendas de um cliente")
    public ResponseEntity<List<VendaDTO>> listarPorCliente(@PathVariable Integer cdCliente) {
        List<VendaDTO> response = vendaService.listarPorCliente(cdCliente);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/atendente/{cdAtendente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar vendas de um atendente")
    public ResponseEntity<List<VendaDTO>> listarPorAtendente(@PathVariable Integer cdAtendente) {
        List<VendaDTO> response = vendaService.listarPorAtendente(cdAtendente);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar vendas por período")
    public ResponseEntity<List<VendaDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<VendaDTO> response = vendaService.listarPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-dia")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Calcular total de vendas do dia")
    public ResponseEntity<Map<String, Double>> calcularTotalDia() {
        Double total = vendaService.calcularTotalVendasDoDia();
        return ResponseEntity.ok(Map.of("totalDia", total));
    }
}
