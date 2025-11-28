package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.VeiculoDTO;
import com.oficinamecanica.OficinaMecanica.services.VeiculoService;
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
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cadastrar novo veículo")
    public ResponseEntity<VeiculoResponseDTO> criar(@Valid @RequestBody VeiculoDTO dto) {
        VeiculoResponseDTO response = veiculoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<VeiculoResponseDTO> buscarPorId(@PathVariable Integer id) {
        VeiculoResponseDTO response = veiculoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar todos os veículos")
    public ResponseEntity<List<VeiculoResponseDTO>> listarTodos() {
        List<VeiculoResponseDTO> response = veiculoService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cliente/{cdCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar veículos de um cliente")
    public ResponseEntity<List<VeiculoResponseDTO>> listarPorCliente(@PathVariable Integer cdCliente) {
        List<VeiculoResponseDTO> response = veiculoService.listarPorCliente(cdCliente);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualizar veículo")
    public ResponseEntity<VeiculoResponseDTO> atualizar(@PathVariable Integer id,
                                                        @Valid @RequestBody VeiculoDTO dto) {
        VeiculoResponseDTO response = veiculoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Deletar veículo")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        veiculoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
