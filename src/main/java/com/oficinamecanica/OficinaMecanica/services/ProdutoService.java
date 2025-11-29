package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ProdutoDTO;
import com.oficinamecanica.OficinaMecanica.models.ProdutoModel;
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
        ProdutoModel produto = ProdutoModel.builder()
                .nmProduto(dto.nmProduto())
                .dsProduto(dto.dsProduto())
                .categoria(dto.categoria())
                .vlProduto(dto.vlProduto()) // ✅ CORRIGIDO: Produto só tem vlProduto
                .qtdEstoque(dto.qtdEstoque())
                .qtdMinimoEstoque(dto.qtdMinimo()) // ✅ CORRIGIDO: nome do campo no model
                .ativo(true)
                .build();

        ProdutoModel salvo = produtoRepository.save(produto);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ProdutoDTO buscarPorId(Integer id) {
        ProdutoModel produto = produtoRepository.findById(id)
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
        ProdutoModel produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setNmProduto(dto.nmProduto());
        produto.setDsProduto(dto.dsProduto());
        produto.setCategoria(dto.categoria());
        produto.setVlProduto(dto.vlProduto());
        produto.setQtdEstoque(dto.qtdEstoque());
        produto.setQtdMinimoEstoque(dto.qtdMinimo());


        ProdutoModel atualizado = produtoRepository.save(produto);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        ProdutoModel produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    // ✅ CONVERTER ENTIDADE → DTO
    private ProdutoDTO converterParaDTO(ProdutoModel produto) {
        return new ProdutoDTO(
                produto.getCdProduto(),
                produto.getNmProduto(),
                produto.getDsProduto(),
                produto.getCategoria(),
                produto.getVlProduto(), // venda
                produto.getQtdEstoque(),
                produto.getQtdMinimoEstoque(),
                produto.getAtivo()
        );
    }
}