package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.ProdutoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<ProdutoModel, Integer> {

    List<ProdutoModel> findByAtivoTrue();

    @Query("SELECT p FROM ProdutoModel p WHERE p.qtdEstoque < p.qtdMinimoEstoque AND p.ativo = true")
    List<ProdutoModel> findProdutosComEstoqueBaixo();

    @Query("SELECT p FROM ProdutoModel p WHERE p.qtdEstoque > 0 AND p.ativo = true")
    List<ProdutoModel> findProdutosDisponiveis();
}