package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.OrdemServicoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.OrdemServicoResponseDTO;
import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import com.oficinamecanica.OficinaMecanica.enums.StatusOrdemServico;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
import com.oficinamecanica.OficinaMecanica.models.*;
import com.oficinamecanica.OficinaMecanica.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final ServicoRepository servicoRepository;
    private final ItemOrdemServicoRepository itemOrdemServicoRepository;
    private final FaturamentoRepository faturamentoRepository;

    @Transactional
    public OrdemServicoResponseDTO criar(OrdemServicoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getCdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Veiculo veiculo = veiculoRepository.findById(dto.getCdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        Usuario mecanico = usuarioRepository.findById(dto.getCdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        OrdemServico ordem = OrdemServico.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .tipoServico(dto.getTipoServico())
                .statusOrdemServico(StatusOrdemServico.AGUARDANDO)
                .dataAbertura(LocalDateTime.now())
                .vlPecas(0.0)
                .vlMaoObra(dto.getVlMaoObra() != null ? dto.getVlMaoObra() : 0.0)
                .vlTotal(0.0)
                .desconto(dto.getDesconto() != null ? dto.getDesconto() : 0.0)
                .observacoes(dto.getObservacoes())
                .diagnostico(dto.getDiagnostico())
                .aprovado(false)
                .build();

        OrdemServico salva = ordemServicoRepository.save(ordem);

        // Adicionar itens se fornecidos
        if (dto.getItens() != null && !dto.getItens().isEmpty()) {
            adicionarItens(salva, dto.getItens());
        }

        return converterParaDTO(salva);
    }

    @Transactional
    public void adicionarItens(OrdemServico ordem, List<OrdemServicoRequestDTO.ItemDTO> itensDTO) {
        double totalPecas = 0.0;

        for (OrdemServicoRequestDTO.ItemDTO itemDTO : itensDTO) {
            ItemOrdemServico item = new ItemOrdemServico();
            item.setOrdemServico(ordem);
            item.setQuantidade(itemDTO.getQuantidade());

            if (itemDTO.getCdProduto() != null) {
                Produto produto = produtoRepository.findById(itemDTO.getCdProduto())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
                
                // Validar estoque
                if (produto.getQtdEstoque() < itemDTO.getQuantidade()) {
                    throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNmProduto());
                }

                item.setProduto(produto);
                item.setVlUnitario(produto.getVlVenda());
                item.setVlTotal(produto.getVlVenda() * itemDTO.getQuantidade());
                totalPecas += item.getVlTotal();

                // Dar baixa no estoque se não for orçamento
                if (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO) {
                    produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.getQuantidade());
                    produtoRepository.save(produto);
                }
            }

            if (itemDTO.getCdServico() != null) {
                Servico servico = servicoRepository.findById(itemDTO.getCdServico())
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
                
                item.setServico(servico);
                item.setVlUnitario(servico.getVlServico());
                item.setVlTotal(servico.getVlServico() * itemDTO.getQuantidade());
            }

            itemOrdemServicoRepository.save(item);
        }

        // Atualizar valores da ordem
        ordem.setVlPecas(totalPecas);
        ordem.setVlTotal(ordem.getVlPecas() + ordem.getVlMaoObra() - ordem.getDesconto());
        ordemServicoRepository.save(ordem);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorId(Integer id) {
        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));
        return converterParaDTO(ordem);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorStatus(StatusOrdemServico status) {
        return ordemServicoRepository.findByStatusOrdemServico(status).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarOrcamentosPendentes() {
        return ordemServicoRepository.findOrcamentosPendentes().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrdemServicoResponseDTO aprovarOrcamento(Integer id) {
        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        if (ordem.getTipoServico() != TipoServico.ORCAMENTO) {
            throw new RuntimeException("Apenas orçamentos podem ser aprovados");
        }

        ordem.setAprovado(true);
        ordem.setTipoServico(TipoServico.ORDEM_DE_SERVICO);
        ordem.setStatusOrdemServico(StatusOrdemServico.AGUARDANDO);

        // Dar baixa no estoque dos produtos
        List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServico_CdOrdemServico(id);
        for (ItemOrdemServico item : itens) {
            if (item.getProduto() != null) {
                Produto produto = item.getProduto();
                if (produto.getQtdEstoque() < item.getQuantidade()) {
                    throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNmProduto());
                }
                produto.setQtdEstoque(produto.getQtdEstoque() - item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        OrdemServico atualizada = ordemServicoRepository.save(ordem);
        return converterParaDTO(atualizada);
    }

    @Transactional
    public OrdemServicoResponseDTO concluir(Integer id, String formaPagamento) {
        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        ordem.setStatusOrdemServico(StatusOrdemServico.CONCLUIDA);
        ordem.setDataFechamento(LocalDateTime.now());

        OrdemServico concluida = ordemServicoRepository.save(ordem);

        // Gerar faturamento automaticamente
        gerarFaturamento(concluida, formaPagamento);

        return converterParaDTO(concluida);
    }

    @Transactional
    public void gerarFaturamento(OrdemServico ordem, String formaPagamento) {
        Faturamento faturamento = Faturamento.builder()
                .ordemServico(ordem)
                .dataVenda(ordem.getDataFechamento())
                .vlTotal(ordem.getVlTotal())
                .formaPagamento(FormaPagamento.valueOf(formaPagamento))
                .build();

        faturamentoRepository.save(faturamento);
    }

    @Transactional
    public void cancelar(Integer id) {
        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        // Devolver produtos ao estoque se já foi dado baixa
        if (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO) {
            List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServico_CdOrdemServico(id);
            for (ItemOrdemServico item : itens) {
                if (item.getProduto() != null) {
                    Produto produto = item.getProduto();
                    produto.setQtdEstoque(produto.getQtdEstoque() + item.getQuantidade());
                    produtoRepository.save(produto);
                }
            }
        }

        ordem.setStatusOrdemServico(StatusOrdemServico.CANCELADA);
        ordemServicoRepository.save(ordem);
    }

    private OrdemServicoResponseDTO converterParaDTO(OrdemServico ordem) {
        return OrdemServicoResponseDTO.builder()
                .cdOrdemServico(ordem.getCdOrdemServico())
                .cdCliente(ordem.getCliente().getCdCliente())
                .nmCliente(ordem.getCliente().getNmCliente())
                .cdVeiculo(ordem.getVeiculo().getCdVeiculo())
                .placa(ordem.getVeiculo().getPlaca())
                .cdMecanico(ordem.getMecanico().getCdUsuario())
                .nmMecanico(ordem.getMecanico().getNmUsuario())
                .tipoServico(ordem.getTipoServico())
                .statusOrdemServico(ordem.getStatusOrdemServico())
                .dataAbertura(ordem.getDataAbertura())
                .dataFechamento(ordem.getDataFechamento())
                .vlPecas(ordem.getVlPecas())
                .vlMaoObra(ordem.getVlMaoObra())
                .vlTotal(ordem.getVlTotal())
                .desconto(ordem.getDesconto())
                .observacoes(ordem.getObservacoes())
                .diagnostico(ordem.getDiagnostico())
                .aprovado(ordem.getAprovado())
                .build();
    }
}
