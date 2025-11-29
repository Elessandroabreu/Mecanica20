package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.OrdemServicoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.OrdemServicoResponseDTO;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar todas as ordens de serviço")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ordemServicoService.listarTodas());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Criar nova ordem de serviço ou orçamento")
    public ResponseEntity<OrdemServicoResponseDTO> criar(@Valid @RequestBody OrdemServicoRequestDTO dto) {
        OrdemServicoResponseDTO response = ordemServicoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    @Operation(summary = "Iniciar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> iniciar(@PathVariable Integer id) {
        return ResponseEntity.ok(ordemServicoService.iniciar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualizar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody OrdemServicoRequestDTO dto) {

        OrdemServicoResponseDTO response = ordemServicoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar ordem de serviço por ID")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ordemServicoService.buscarPorId(id));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar ordens por status")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarPorStatus(@PathVariable Status status) {
        return ResponseEntity.ok(ordemServicoService.listarPorStatus(status));
    }

    @GetMapping("/orcamentos/pendentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar orçamentos pendentes de aprovação")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarOrcamentosPendentes() {
        return ResponseEntity.ok(ordemServicoService.listarOrcamentosPendentes());
    }

    @PatchMapping("/{id}/aprovar-orcamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Aprovar orçamento e converter em ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> aprovarOrcamento(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        String dataStr = body.get("dataAgendamento");
        LocalDate dataAgendamento = (dataStr != null ? LocalDate.parse(dataStr) : null);

        return ResponseEntity.ok(ordemServicoService.aprovarOrcamento(id, dataAgendamento));
    }

    @PatchMapping("/{cdOrdemServico}/diagnostico-e-mao-obra")
    public ResponseEntity<OrdemServicoResponseDTO> atualizarDiagnosticoEMaoObra(
            @PathVariable Integer cdOrdemServico,
            @RequestBody Map<String, Object> dados
    ) {
        String diagnostico = dados.get("diagnostico") != null
                ? dados.get("diagnostico").toString()
                : "";

        Double vlMaoObraExtra = dados.get("vlMaoObraExtra") != null
                ? Double.parseDouble(dados.get("vlMaoObraExtra").toString())
                : 0.0;

        OrdemServicoResponseDTO resultado = ordemServicoService
                .atualizarDiagnosticoEMaoObra(cdOrdemServico, diagnostico, vlMaoObraExtra);

        return ResponseEntity.ok(resultado);
    }

    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Concluir ordem de serviço e gerar faturamento")
    public ResponseEntity<OrdemServicoResponseDTO> concluir(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        String formaPagamento = body.get("formaPagamento");
        return ResponseEntity.ok(ordemServicoService.concluir(id, formaPagamento));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cancelar ordem de serviço e devolver produtos ao estoque")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        ordemServicoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
