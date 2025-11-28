package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.request.OrdemServicoDTO;
import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.services.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Endpoints para gerenciamento de ordens de serviço")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Criar nova ordem de serviço ou orçamento")
    public ResponseEntity<OrdemServicoResponseDTO> criar(@Valid @RequestBody OrdemServicoDTO dto) {
        OrdemServicoResponseDTO response = ordemServicoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    @Operation(summary = "Iniciar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> iniciar(@PathVariable Integer id) {
        OrdemServicoResponseDTO response = ordemServicoService.iniciar(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualizar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody OrdemServicoDTO dto) {
        OrdemServicoResponseDTO response = ordemServicoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar ordem de serviço por ID")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Integer id) {
        OrdemServicoResponseDTO response = ordemServicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar ordens por status")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarPorStatus(@PathVariable Status status) {
        List<OrdemServicoResponseDTO> response = ordemServicoService.listarPorStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orcamentos/pendentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar orçamentos pendentes de aprovação")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarOrcamentosPendentes() {
        List<OrdemServicoResponseDTO> response = ordemServicoService.listarOrcamentosPendentes();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/aprovar-orcamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Aprovar orçamento e converter em ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> aprovarOrcamento(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        // ✅ Receber dataAgendamento do body
        String dataStr = body.get("dataAgendamento");
        LocalDate dataAgendamento = dataStr != null ? LocalDate.parse(dataStr) : null;

        OrdemServicoResponseDTO response = ordemServicoService.aprovarOrcamento(id, dataAgendamento);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Concluir ordem de serviço e gerar faturamento")
    public ResponseEntity<OrdemServicoResponseDTO> concluir(@PathVariable Integer id,
                                                             @RequestBody Map<String, String> body) {
        String formaPagamento = body.get("formaPagamento");
        OrdemServicoResponseDTO response = ordemServicoService.concluir(id, formaPagamento);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cancelar ordem de serviço e devolver produtos ao estoque")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        ordemServicoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
