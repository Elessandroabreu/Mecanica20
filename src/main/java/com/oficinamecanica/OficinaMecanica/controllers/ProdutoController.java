package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.request.ProdutoDTO;
import com.oficinamecanica.OficinaMecanica.services.ProdutoService;
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
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos/peças")
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Cadastrar novo produto")
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoDTO dto) {
        ProdutoResponseDTO response = produtoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Integer id) {
        ProdutoResponseDTO response = produtoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar produtos ativos")
    public ResponseEntity<List<ProdutoResponseDTO>> listarAtivos() {
        List<ProdutoResponseDTO> response = produtoService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estoque-baixo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Listar produtos com estoque baixo")
    public ResponseEntity<List<ProdutoResponseDTO>> listarEstoqueBaixo() {
        List<ProdutoResponseDTO> response = produtoService.listarComEstoqueBaixo();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Integer id,
                                                        @Valid @RequestBody ProdutoDTO dto) {
        ProdutoResponseDTO response = produtoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Deletar produto (soft delete)")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
