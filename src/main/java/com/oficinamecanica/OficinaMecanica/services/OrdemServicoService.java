package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.OrdemServicoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.ItemOrdemServicoResponseDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.OrdemServicoResponseDTO;
import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import com.oficinamecanica.OficinaMecanica.enums.StatusOrdemServico;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
// Usando RuntimeException padrão do projeto
import com.oficinamecanica.OficinaMecanica.models.*;
import com.oficinamecanica.OficinaMecanica.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("Criando nova ordem de serviço para cliente: {}", dto.cdCliente());

        Cliente cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + dto.cdCliente()));

        Veiculo veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com ID: " + dto.cdVeiculo()));

        Usuario mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado com ID: " + dto.cdMecanico()));

        OrdemServico ordem = OrdemServico.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .tipoServico(dto.tipoServico())
                .statusOrdemServico(StatusOrdemServico.AGUARDANDO)
                .dataAbertura(LocalDateTime.now())
                .vlPecas(0.0)
                .vlMaoObra(dto.vlMaoObra() != null ? dto.vlMaoObra() : 0.0)
                .vlTotal(0.0)
                .desconto(dto.desconto() != null ? dto.desconto() : 0.0)
                .observacoes(dto.observacoes())
                .diagnostico(dto.diagnostico())
                .aprovado(false)
                .build();

        OrdemServico salva = ordemServicoRepository.save(ordem);
        log.info("Ordem de serviço criada com ID: {}", salva.getCdOrdemServico());

        if (dto.itens() != null && !dto.itens().isEmpty()) {
            adicionarItens(salva, dto.itens());
        }

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(salva.getCdOrdemServico()));
    }

    @Transactional
    public void adicionarItens(OrdemServico ordem, List<OrdemServicoRequestDTO.ItemDTO> itensDTO) {
        double totalPecas = 0.0;

        for (OrdemServicoRequestDTO.ItemDTO itemDTO : itensDTO) {
            ItemOrdemServico item = new ItemOrdemServico();
            item.setOrdemServico(ordem);
            item.setQuantidade(itemDTO.quantidade());

            if (itemDTO.cdProduto() != null) {
                Produto produto = produtoRepository.findById(itemDTO.cdProduto())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.cdProduto()));

                if (produto.getQtdEstoque() < itemDTO.quantidade()) {
                    throw new RuntimeException(
                            "Estoque insuficiente para " + produto.getNmProduto() +
                                    ". Disponível: " + produto.getQtdEstoque() +
                                    ", Solicitado: " + itemDTO.quantidade()
                    );
                }

                item.setProduto(produto);
                item.setVlUnitario(produto.getVlVenda());
                item.setVlTotal(produto.getVlVenda() * itemDTO.quantidade());
                totalPecas += item.getVlTotal();

                // Só dá baixa se for ordem de serviço (não orçamento)
                if (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO) {
                    produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.quantidade());
                    produtoRepository.save(produto);
                    log.debug("Estoque atualizado para produto {}: {}", produto.getNmProduto(), produto.getQtdEstoque());
                }
            }

            if (itemDTO.cdServico() != null) {
                Servico servico = servicoRepository.findById(itemDTO.cdServico())
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado: " + itemDTO.cdServico()));

                item.setServico(servico);
                item.setVlUnitario(servico.getVlServico());
                item.setVlTotal(servico.getVlServico() * itemDTO.quantidade());
            }

            itemOrdemServicoRepository.save(item);
        }

        ordem.setVlPecas(totalPecas);
        ordem.setVlTotal(ordem.getVlPecas() + ordem.getVlMaoObra() - ordem.getDesconto());
        ordemServicoRepository.save(ordem);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorId(Integer id) {
        log.debug("Buscando ordem de serviço com ID: {}", id);
        OrdemServico ordem = ordemServicoRepository.findByIdWithItens(id);

        if (ordem == null) {
            throw new RuntimeException("Ordem de serviço não encontrada com ID: " + id);
        }

        return converterParaDTO(ordem);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorStatus(StatusOrdemServico status) {
        log.debug("Listando ordens com status: {}", status);
        return ordemServicoRepository.findByStatusOrdemServico(status).stream()
                .map(ordem -> converterParaDTO(
                        ordemServicoRepository.findByIdWithItens(ordem.getCdOrdemServico())
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarOrcamentosPendentes() {
        return ordemServicoRepository.findOrcamentosPendentes().stream()
                .map(ordem -> converterParaDTO(
                        ordemServicoRepository.findByIdWithItens(ordem.getCdOrdemServico())
                ))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public OrdemServicoResponseDTO aprovarOrcamento(Integer id) {
        log.info("Aprovando orçamento ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getTipoServico() != TipoServico.ORCAMENTO) {
            throw new RuntimeException("Apenas orçamentos podem ser aprovados");
        }

        // Validar estoque ANTES de aprovar
        List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServico_CdOrdemServico(id);
        for (ItemOrdemServico item : itens) {
            if (item.getProduto() != null) {
                Produto produto = item.getProduto();
                if (produto.getQtdEstoque() < item.getQuantidade()) {
                    throw new RuntimeException(
                            "Estoque insuficiente para aprovar orçamento. Produto: " + produto.getNmProduto()
                    );
                }
            }
        }

        // Aprovar e dar baixa no estoque
        ordem.setAprovado(true);
        ordem.setTipoServico(TipoServico.ORDEM_DE_SERVICO);
        ordem.setStatusOrdemServico(StatusOrdemServico.AGUARDANDO);

        for (ItemOrdemServico item : itens) {
            if (item.getProduto() != null) {
                Produto produto = item.getProduto();
                produto.setQtdEstoque(produto.getQtdEstoque() - item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        OrdemServico atualizada = ordemServicoRepository.save(ordem);
        log.info("Orçamento aprovado e convertido em ordem de serviço: {}", id);

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    @Transactional(rollbackFor = Exception.class)
    public OrdemServicoResponseDTO concluir(Integer id, String formaPagamento) {
        log.info("Concluindo ordem de serviço ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getStatusOrdemServico() == StatusOrdemServico.CONCLUIDA) {
            throw new RuntimeException("Ordem de serviço já foi concluída");
        }

        ordem.setStatusOrdemServico(StatusOrdemServico.CONCLUIDA);
        ordem.setDataFechamento(LocalDateTime.now());

        OrdemServico concluida = ordemServicoRepository.save(ordem);
        gerarFaturamento(concluida, formaPagamento);

        log.info("Ordem de serviço concluída: {}", id);
        return converterParaDTO(ordemServicoRepository.findByIdWithItens(concluida.getCdOrdemServico()));
    }

    @Transactional
    protected void gerarFaturamento(OrdemServico ordem, String formaPagamento) {
        try {
            Faturamento faturamento = Faturamento.builder()
                    .ordemServico(ordem)
                    .dataVenda(ordem.getDataFechamento())
                    .vlTotal(ordem.getVlTotal())
                    .formaPagamento(FormaPagamento.valueOf(formaPagamento))
                    .build();

            faturamentoRepository.save(faturamento);
            log.info("Faturamento gerado para ordem {}: R$ {}", ordem.getCdOrdemServico(), ordem.getVlTotal());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Forma de pagamento inválida: " + formaPagamento);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelar(Integer id) {
        log.info("Cancelando ordem de serviço ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getStatusOrdemServico() == StatusOrdemServico.CONCLUIDA) {
            throw new RuntimeException("Não é possível cancelar ordem de serviço concluída");
        }

        // Devolver produtos ao estoque apenas se foi ordem de serviço (não orçamento)
        if (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO) {
            List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServico_CdOrdemServico(id);
            for (ItemOrdemServico item : itens) {
                if (item.getProduto() != null) {
                    Produto produto = item.getProduto();
                    produto.setQtdEstoque(produto.getQtdEstoque() + item.getQuantidade());
                    produtoRepository.save(produto);
                    log.debug("Estoque devolvido para produto {}: {}", produto.getNmProduto(), produto.getQtdEstoque());
                }
            }
        }

        ordem.setStatusOrdemServico(StatusOrdemServico.CANCELADA);
        ordemServicoRepository.save(ordem);
        log.info("Ordem de serviço cancelada: {}", id);
    }

    private ItemOrdemServicoResponseDTO converterItemParaDTO(ItemOrdemServico item) {
        return new ItemOrdemServicoResponseDTO(
                item.getCdItemOrdemServico(),
                item.getProduto() != null ? item.getProduto().getCdProduto() : null,
                item.getProduto() != null ? item.getProduto().getNmProduto() : null,
                item.getServico() != null ? item.getServico().getCdServico() : null,
                item.getServico() != null ? item.getServico().getNmServico() : null,
                item.getQuantidade(),
                item.getVlUnitario(),
                item.getVlTotal()
        );
    }

    private OrdemServicoResponseDTO converterParaDTO(OrdemServico ordem) {
        // Usar itens já carregados pelo fetch join
        List<ItemOrdemServicoResponseDTO> itensDTO = ordem.getItens().stream()
                .map(this::converterItemParaDTO)
                .collect(Collectors.toList());

        return new OrdemServicoResponseDTO(
                ordem.getCdOrdemServico(),
                ordem.getCliente().getCdCliente(),
                ordem.getCliente().getNmCliente(),
                ordem.getVeiculo().getCdVeiculo(),
                ordem.getVeiculo().getPlaca(),
                ordem.getMecanico().getCdUsuario(),
                ordem.getMecanico().getNmUsuario(),
                ordem.getTipoServico(),
                ordem.getStatusOrdemServico(),
                ordem.getDataAbertura(),
                ordem.getDataFechamento(),
                ordem.getVlPecas(),
                ordem.getVlMaoObra(),
                ordem.getVlTotal(),
                ordem.getDesconto(),
                ordem.getObservacoes(),
                ordem.getDiagnostico(),
                ordem.getAprovado(),
                itensDTO
        );
    }
}