package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.OrdemServicoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.ItemOrdemServicoResponseDTO;
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
        Cliente cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Veiculo veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        Usuario mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

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

        if (dto.itens() != null && !dto.itens().isEmpty()) {
            adicionarItens(salva, dto.itens());
        }

        return converterParaDTO(salva);
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
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

                if (produto.getQtdEstoque() < itemDTO.quantidade()) {
                    throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNmProduto());
                }

                item.setProduto(produto);
                item.setVlUnitario(produto.getVlVenda());
                item.setVlTotal(produto.getVlVenda() * itemDTO.quantidade());
                totalPecas += item.getVlTotal();

                if (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO) {
                    produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.quantidade());
                    produtoRepository.save(produto);
                }
            }

            if (itemDTO.cdServico() != null) {
                Servico servico = servicoRepository.findById(itemDTO.cdServico())
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

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
        // ✅ USAR FETCH JOIN PARA BUSCAR OS ITENS
        OrdemServico ordem = ordemServicoRepository.findByIdWithItens(id);
        if (ordem == null) {
            throw new RuntimeException("Ordem de serviço não encontrada");
        }
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
        // Buscar os itens da ordem
        List<ItemOrdemServico> itens = itemOrdemServicoRepository
                .findByOrdemServico_CdOrdemServico(ordem.getCdOrdemServico());

        // Converter os itens para DTO
        List<ItemOrdemServicoResponseDTO> itensDTO = itens.stream()
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
                itensDTO // ✅ INCLUIR OS ITENS
        );
    }
}