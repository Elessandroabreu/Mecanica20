package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.AgendamentoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.AgendamentoResponseDTO;
import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.services.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE' , 'MECANICO')")
    @Operation(summary = "Criar novo agendamento")
    public ResponseEntity<AgendamentoResponseDTO> criar(@Valid @RequestBody AgendamentoRequestDTO dto) {
        AgendamentoResponseDTO response = agendamentoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar agendamento por ID")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(@PathVariable Integer id) {
        AgendamentoResponseDTO response = agendamentoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar todos os agendamentos")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarTodos() {
        List<AgendamentoResponseDTO> response = agendamentoService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mecanico/{cdMecanico}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar agendamentos de um mecânico")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarPorMecanico(@PathVariable Integer cdMecanico) {
        List<AgendamentoResponseDTO> response = agendamentoService.listarPorMecanico(cdMecanico);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/futuros")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE','MECANICO')")
    @Operation(summary = "Listar agendamentos futuros")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarFuturos() {
        List<AgendamentoResponseDTO> response = agendamentoService.listarAgendamentosFuturos();
        return ResponseEntity.ok(response);
    }

     @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE','MECANICO')")
    @Operation(summary = "Atualizar agendamento")
    public ResponseEntity<AgendamentoResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AgendamentoRequestDTO dto) {
        AgendamentoResponseDTO response = agendamentoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

     @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Atualizar status do agendamento")
    public ResponseEntity<AgendamentoResponseDTO> atualizarStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        String statusStr = body.get("status");
        Status novoStatus = Status.valueOf(statusStr);

        AgendamentoResponseDTO response = agendamentoService.atualizarStatus(id, novoStatus);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cancelar agendamento")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        agendamentoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}