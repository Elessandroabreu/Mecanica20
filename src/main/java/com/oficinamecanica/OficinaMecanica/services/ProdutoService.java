package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ProdutoDTO;
import com.oficinamecanica.OficinaMecanica.models.Produto;
import com.oficinamecanica.OficinaMecanica.repositories.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional
    public ProdutoDTO criar(ProdutoDTO dto) {
        Produto produto = Produto.builder()
                .nmProduto(dto.nmProduto())
                .dsProduto(dto.dsProduto())
                .categoria(dto.categoria())
                .vlProduto(dto.vlVenda()) // ✅ CORRIGIDO: Produto só tem vlProduto
                .qtdEstoque(dto.qtdEstoque())
                .qtdMinimoEstoque(dto.qtdMinimo()) // ✅ CORRIGIDO: nome do campo no model
                .ativo(true)
                .build();

        Produto salvo = produtoRepository.save(produto);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ProdutoDTO buscarPorId(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return converterParaDTO(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoDTO> listarAtivos() {
        return produtoRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoDTO> listarComEstoqueBaixo() {
        return produtoRepository.findProdutosComEstoqueBaixo().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoDTO atualizar(Integer id, ProdutoDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setNmProduto(dto.nmProduto());
        produto.setDsProduto(dto.dsProduto());
        produto.setCategoria(dto.categoria());
        produto.setVlProduto(dto.vlVenda()); // ✅ CORRIGIDO
        produto.setQtdEstoque(dto.qtdEstoque());
        produto.setQtdMinimoEstoque(dto.qtdMinimo()); // ✅ CORRIGIDO

        Produto atualizado = produtoRepository.save(produto);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    // ✅ CONVERTER ENTIDADE → DTO
    private ProdutoDTO converterParaDTO(Produto produto) {
        return new ProdutoDTO(
                produto.getCdProduto(),
                produto.getNmProduto(),
                produto.getDsProduto(),
                produto.getCategoria(),
                produto.getVlProduto(), // custo = venda (seu model só tem vlProduto)
                produto.getVlProduto(), // venda
                produto.getQtdEstoque(),
                produto.getQtdMinimoEstoque(),
                produto.getAtivo()
        );
    }
}