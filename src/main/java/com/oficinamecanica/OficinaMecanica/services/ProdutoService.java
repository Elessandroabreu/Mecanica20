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
                .nmProduto(dto.nmProduto())
                .dsProduto(dto.dsProduto())
                .categoria(dto.categoria())
                .vlCusto(dto.vlCusto())
                .vlVenda(dto.vlVenda())
                .qtdEstoque(dto.qtdEstoque())
                .qtdMinimo(dto.qtdMinimo())
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

        produto.setNmProduto(dto.nmProduto());
        produto.setDsProduto(dto.dsProduto());
        produto.setCategoria(dto.categoria());
        produto.setVlCusto(dto.vlCusto());
        produto.setVlVenda(dto.vlVenda());
        produto.setQtdEstoque(dto.qtdEstoque());
        produto.setQtdMinimo(dto.qtdMinimo());

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
        return new ProdutoResponseDTO(
                produto.getCdProduto(),
                produto.getNmProduto(),
                produto.getDsProduto(),
                produto.getCategoria(),
                produto.getVlCusto(),
                produto.getVlVenda(),
                produto.getQtdEstoque(),
                produto.getQtdMinimo(),
                produto.getAtivo(),
                produto.getDataCadastro()
        );
    }
}