package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    // Listar apenas produtos ativos
    List<Produto> findByAtivoTrue();

    // Buscar produtos com estoque baixo (menor que mínimo)
    @Query("SELECT p FROM Produto p WHERE p.qtdEstoque < p.qtdMinimoEstoque AND p.ativo = true")
    List<Produto> findProdutosComEstoqueBaixo();

    // Buscar produtos disponíveis (com estoque e ativos)
    @Query("SELECT p FROM Produto p WHERE p.qtdEstoque > 0 AND p.ativo = true")
    List<Produto> findProdutosDisponiveis();
}