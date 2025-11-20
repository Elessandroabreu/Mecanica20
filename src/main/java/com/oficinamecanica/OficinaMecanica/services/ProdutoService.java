package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.ProdutoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.ProdutoResponseDTO;
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
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Produto produto = Produto.builder()
                .nmProduto(dto.getNmProduto())
                .dsProduto(dto.getDsProduto())
                .categoria(dto.getCategoria())
                .vlCusto(dto.getVlCusto())
                .vlVenda(dto.getVlVenda())
                .qtdEstoque(dto.getQtdEstoque())
                .qtdMinimo(dto.getQtdMinimo())
                .ativo(true)
                .build();

        Produto salvo = produtoRepository.save(produto);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return converterParaDTO(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarAtivos() {
        return produtoRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarComEstoqueBaixo() {
        return produtoRepository.findProdutosComEstoqueBaixo().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarDisponiveis() {
        return produtoRepository.findProdutosDisponiveis().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Integer id, ProdutoRequestDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setNmProduto(dto.getNmProduto());
        produto.setDsProduto(dto.getDsProduto());
        produto.setCategoria(dto.getCategoria());
        produto.setVlCusto(dto.getVlCusto());
        produto.setVlVenda(dto.getVlVenda());
        produto.setQtdEstoque(dto.getQtdEstoque());
        produto.setQtdMinimo(dto.getQtdMinimo());

        Produto atualizado = produtoRepository.save(produto);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        
        // Soft delete
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional
    public void darBaixaEstoque(Integer cdProduto, Integer quantidade) {
        Produto produto = produtoRepository.findById(cdProduto)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getQtdEstoque() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }

        produto.setQtdEstoque(produto.getQtdEstoque() - quantidade);
        produtoRepository.save(produto);
    }

    @Transactional
    public void adicionarEstoque(Integer cdProduto, Integer quantidade) {
        Produto produto = produtoRepository.findById(cdProduto)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setQtdEstoque(produto.getQtdEstoque() + quantidade);
        produtoRepository.save(produto);
    }

    private ProdutoResponseDTO converterParaDTO(Produto produto) {
        return ProdutoResponseDTO.builder()
                .cdProduto(produto.getCdProduto())
                .nmProduto(produto.getNmProduto())
                .dsProduto(produto.getDsProduto())
                .categoria(produto.getCategoria())
                .vlCusto(produto.getVlCusto())
                .vlVenda(produto.getVlVenda())
                .qtdEstoque(produto.getQtdEstoque())
                .qtdMinimo(produto.getQtdMinimo())
                .ativo(produto.getAtivo())
                .dataCadastro(produto.getDataCadastro())
                .build();
    }
}
