package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    // Listar apenas produtos ativos
    List<Produto> findByAtivoTrue();

    // Listar produtos inativos
    List<Produto> findByAtivoFalse();

    // Buscar por nome
    List<Produto> findByNmProdutoContainingIgnoreCase(String nmProduto);

    // Buscar por categoria
    List<Produto> findByCategoria(String categoria);

    // Buscar produtos com estoque baixo (menor que mínimo)
    @Query("SELECT p FROM Produto p WHERE p.qtdEstoque < p.qtdMinimo AND p.ativo = true")
    List<Produto> findProdutosComEstoqueBaixo();

    // Buscar produtos com estoque zerado
    @Query("SELECT p FROM Produto p WHERE p.qtdEstoque = 0 AND p.ativo = true")
    List<Produto> findProdutosSemEstoque();

    // Buscar produtos disponíveis (com estoque e ativos)
    @Query("SELECT p FROM Produto p WHERE p.qtdEstoque > 0 AND p.ativo = true")
    List<Produto> findProdutosDisponiveis();

    // Buscar por categoria e ativos
    List<Produto> findByCategoriaAndAtivoTrue(String categoria);

    // Listar todas as categorias distintas
    @Query("SELECT DISTINCT p.categoria FROM Produto p WHERE p.ativo = true")
    List<String> findAllCategorias();
}
