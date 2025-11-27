package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.OrdemServicoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.ItemOrdemServicoResponseDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.OrdemServicoResponseDTO;
import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import com.oficinamecanica.OficinaMecanica.enums.StatusOrdemServico;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
import com.oficinamecanica.OficinaMecanica.models.*;
import com.oficinamecanica.OficinaMecanica.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final AgendamentoRepository agendamentoRepository;

    @Transactional
    public OrdemServicoResponseDTO criar(OrdemServicoRequestDTO dto) {
        log.info("Criando nova ordem de serviço para cliente: {}", dto.cdCliente());

        // Validar e buscar entidades
        Cliente cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + dto.cdCliente()));

        if (!cliente.getAtivo()) {
            throw new RuntimeException("Cliente inativo não pode criar ordens de serviço");
        }

        Veiculo veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com ID: " + dto.cdVeiculo()));

        Usuario mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado com ID: " + dto.cdMecanico()));

        validarMecanico(mecanico);

        // ✅ VALIDAR DATA DE AGENDAMENTO (se for ordem de serviço)
        if (dto.tipoServico() == TipoServico.ORDEM_DE_SERVICO && dto.dataAgendamento() != null) {
            validarDisponibilidadeMecanico(dto.cdMecanico(), dto.dataAgendamento());
        }

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

        // Adicionar itens
        if (dto.itens() != null && !dto.itens().isEmpty()) {
            adicionarItens(salva, dto.itens());
        }

        // ✅ CRIAR AGENDAMENTO AUTOMÁTICO (se for ordem de serviço e tiver data)
        if (dto.tipoServico() == TipoServico.ORDEM_DE_SERVICO && dto.dataAgendamento() != null) {
            criarAgendamentoAutomatico(salva, dto.dataAgendamento());
        }

        // ✅ CRÍTICO: Buscar novamente COM os itens carregados
        OrdemServico ordemComItens = ordemServicoRepository.findByIdWithItens(salva.getCdOrdemServico());

        // ✅ VERIFICAÇÃO DE SEGURANÇA
        if (ordemComItens == null) {
            throw new RuntimeException("Erro ao carregar ordem de serviço criada");
        }

        return converterParaDTO(ordemComItens);
    }

    // ✅ NOVO MÉTODO: Validar disponibilidade do mecânico
    private void validarDisponibilidadeMecanico(Integer cdMecanico, LocalDate dataAgendamento) {
        // Buscar agendamentos do mecânico para aquele dia
        List<Agendamento> agendamentos = agendamentoRepository
                .findByMecanico_CdUsuarioAndDataAgendamentoAndStatusNot(
                        cdMecanico,
                        dataAgendamento,
                        StatusAgendamento.CANCELADO
                );

        if (!agendamentos.isEmpty()) {
            throw new RuntimeException(
                    "Mecânico já possui agendamento para o dia " + dataAgendamento +
                            ". Escolha outro dia ou outro mecânico."
            );
        }
    }

    // ✅ NOVO MÉTODO: Criar agendamento automático
    private void criarAgendamentoAutomatico(OrdemServico ordem, LocalDate dataAgendamento) {
        Agendamento agendamento = Agendamento.builder()
                .cliente(ordem.getCliente())
                .veiculo(ordem.getVeiculo())
                .mecanico(ordem.getMecanico())
                .dataAgendamento(dataAgendamento)
                .status(StatusAgendamento.AGENDADO)
                .observacoes("Agendamento criado automaticamente da OS #" + ordem.getCdOrdemServico())
                .ordemServico(ordem)
                .build();

        agendamentoRepository.save(agendamento);
        log.info("Agendamento criado automaticamente para OS {} no dia {}",
                ordem.getCdOrdemServico(), dataAgendamento);
    }

    // ✅ MÉTODO CORRIGIDO - Verifica o tipo antes de dar baixa
    @Transactional
    public void adicionarItens(OrdemServico ordem, List<OrdemServicoRequestDTO.ItemDTO> itensDTO) {
        double totalPecas = 0.0;
        boolean darBaixaEstoque = (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO);

        for (OrdemServicoRequestDTO.ItemDTO itemDTO : itensDTO) {
            ItemOrdemServico item = new ItemOrdemServico();
            item.setOrdemServico(ordem);
            item.setQuantidade(itemDTO.quantidade());

            // Processar PRODUTO
            if (itemDTO.cdProduto() != null) {
                Produto produto = produtoRepository.findById(itemDTO.cdProduto())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.cdProduto()));

                // ✅ VALIDAR SE PRODUTO ESTÁ ATIVO
                if (!produto.getAtivo()) {
                    throw new RuntimeException("Produto inativo não pode ser adicionado: " + produto.getNmProduto());
                }

                // Validar estoque disponível (SEMPRE, mesmo para orçamento)
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

                // ✅ SÓ DÁ BAIXA NO ESTOQUE SE FOR ORDEM DE SERVIÇO (NÃO ORÇAMENTO)
                if (darBaixaEstoque) {
                    produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.quantidade());
                    produtoRepository.save(produto);
                    log.debug("Estoque atualizado para produto {}: {}", produto.getNmProduto(), produto.getQtdEstoque());
                } else {
                    log.debug("Orçamento: Estoque NÃO alterado para {}", produto.getNmProduto());
                }
            }

            // Processar SERVIÇO
            if (itemDTO.cdServico() != null) {
                Servico servico = servicoRepository.findById(itemDTO.cdServico())
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado: " + itemDTO.cdServico()));

                // ✅ VALIDAR SE SERVIÇO ESTÁ ATIVO
                if (!servico.getAtivo()) {
                    throw new RuntimeException("Serviço inativo não pode ser adicionado: " + servico.getNmServico());
                }

                item.setServico(servico);
                item.setVlUnitario(servico.getVlServico());
                item.setVlTotal(servico.getVlServico() * itemDTO.quantidade());
            }

            itemOrdemServicoRepository.save(item);
        }

        // Atualizar totais da ordem
        ordem.setVlPecas(totalPecas);
        ordem.setVlTotal(ordem.getVlPecas() + ordem.getVlMaoObra() - ordem.getDesconto());
        ordemServicoRepository.save(ordem);
    }

    // ✅ NOVO MÉTODO: Validar se usuário é mecânico
    private void validarMecanico(Usuario usuario) {
        if (!usuario.getAtivo()) {
            throw new RuntimeException("Mecânico inativo não pode ser atribuído");
        }
        if (!usuario.getRoles().contains(com.oficinamecanica.OficinaMecanica.enums.UserRole.ROLE_MECANICO)) {
            throw new RuntimeException("Usuário " + usuario.getNmUsuario() + " não possui perfil de mecânico");
        }
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

    // ✅ MÉTODO ATUALIZADO - Aprovar orçamento com data de agendamento
    @Transactional(rollbackFor = Exception.class)
    public OrdemServicoResponseDTO aprovarOrcamento(Integer id, LocalDate dataAgendamento) {
        log.info("Aprovando orçamento ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getTipoServico() != TipoServico.ORCAMENTO) {
            throw new RuntimeException("Apenas orçamentos podem ser aprovados");
        }

        if (ordem.getAprovado()) {
            throw new RuntimeException("Orçamento já foi aprovado anteriormente");
        }

        // ✅ VALIDAR DISPONIBILIDADE DO MECÂNICO (se data foi informada)
        if (dataAgendamento != null) {
            validarDisponibilidadeMecanico(ordem.getMecanico().getCdUsuario(), dataAgendamento);
        }

        // Validar e dar baixa no estoque
        List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServico_CdOrdemServico(id);

        for (ItemOrdemServico item : itens) {
            if (item.getProduto() != null) {
                Produto produto = produtoRepository.findById(item.getProduto().getCdProduto())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + item.getProduto().getCdProduto()));

                if (produto.getQtdEstoque() < item.getQuantidade()) {
                    throw new RuntimeException(
                            "Estoque insuficiente para aprovar orçamento. Produto: " + produto.getNmProduto() +
                                    ". Disponível: " + produto.getQtdEstoque() +
                                    ", Necessário: " + item.getQuantidade()
                    );
                }

                produto.setQtdEstoque(produto.getQtdEstoque() - item.getQuantidade());
                produtoRepository.save(produto);
                log.info("Estoque atualizado ao aprovar orçamento - Produto: {}, Novo estoque: {}",
                        produto.getNmProduto(), produto.getQtdEstoque());
            }
        }

        // Aprovar e converter em ordem de serviço
        ordem.setAprovado(true);
        ordem.setTipoServico(TipoServico.ORDEM_DE_SERVICO);
        ordem.setStatusOrdemServico(StatusOrdemServico.AGUARDANDO);

        OrdemServico atualizada = ordemServicoRepository.save(ordem);

        // ✅ CRIAR AGENDAMENTO AUTOMÁTICO (se data foi informada)
        if (dataAgendamento != null) {
            criarAgendamentoAutomatico(atualizada, dataAgendamento);
        }

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

        if (ordem.getStatusOrdemServico() == StatusOrdemServico.CANCELADA) {
            throw new RuntimeException("Ordem de serviço cancelada não pode ser concluída");
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

        if (ordem.getStatusOrdemServico() == StatusOrdemServico.CANCELADA) {
            throw new RuntimeException("Ordem de serviço já está cancelada");
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