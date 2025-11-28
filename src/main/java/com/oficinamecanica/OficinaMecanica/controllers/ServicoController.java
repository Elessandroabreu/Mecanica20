package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.ServicoDTO;
import com.oficinamecanica.OficinaMecanica.services.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de tipos de serviços")
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar novo tipo de serviço")
    public ResponseEntity<ServicoResponseDTO> criar(@Valid @RequestBody ServicoDTO dto) {
        ServicoResponseDTO response = servicoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Integer id) {
        ServicoResponseDTO response = servicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar serviços ativos")
    public ResponseEntity<List<ServicoResponseDTO>> listarAtivos() {
        List<ServicoResponseDTO> response = servicoService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar serviço")
    public ResponseEntity<ServicoResponseDTO> atualizar(@PathVariable Integer id,
                                                        @Valid @RequestBody ServicoDTO dto) {
        ServicoResponseDTO response = servicoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar serviço (soft delete)")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
