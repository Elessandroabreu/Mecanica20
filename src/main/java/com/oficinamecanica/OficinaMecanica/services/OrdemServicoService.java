package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.OrdemServicoDTO;
import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.enums.TipoOrdemOrcamento;
import com.oficinamecanica.OficinaMecanica.models.*;
import com.oficinamecanica.OficinaMecanica.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // ========================================
    // 1️⃣ CRIAR ORÇAMENTO OU ORDEM DE SERVIÇO
    // ========================================
    @Transactional
    public OrdemServicoDTO criar(OrdemServicoDTO dto) {
        log.info("🆕 Criando {} para cliente: {}", dto.tipoOrdemOrcamento(), dto.cdCliente());

        // Validar entidades
        Cliente cliente = buscarClienteAtivo(dto.cdCliente());
        Veiculo veiculo = buscarVeiculo(dto.cdVeiculo());
        Usuario mecanico = buscarMecanicoAtivo(dto.cdMecanico());

        // 🔹 SE FOR ORDEM DE SERVIÇO COM DATA → Validar disponibilidade do mecânico
        if (dto.tipoServico() == TipoOrdemOrcamento.ORDEM_DE_SERVICO && dto.dataAgendamento() != null) {
            validarDisponibilidadeMecanico(dto.cdMecanico(), dto.dataAgendamento());
        }

        // Criar Ordem de Serviço
        OrdemServico ordem = OrdemServico.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .tipoOrdemOrcamento(dto.tipoServico())
                .status(statuss.AGUARDANDO)
                .dataAbertura(LocalDateTime.now())
                .vlPecas(0.0)
                .vlMaoObra(dto.vlMaoObra() != null ? dto.vlMaoObra() : 0.0)
                .vlTotal(0.0)
                .desconto(dto.desconto() != null ? dto.desconto() : 0.0)
                .observacoes(dto.observacoes())
                .diagnostico(dto.diagnostico())
                .aprovado(false)
                .itens(new ArrayList<>()) // ✅ Inicializar lista
                .build();

        OrdemServico salva = ordemServicoRepository.save(ordem);

        // Adicionar itens (produtos e serviços)
        if (dto.itens() != null && !dto.itens().isEmpty()) {
            adicionarItens(salva, dto.itens());
        }

        // 🔹 SE FOR ORDEM DE SERVIÇO COM DATA → Criar agendamento automaticamente
        if (dto.tipoServico() == TipoServico.ORDEM_DE_SERVICO && dto.dataAgendamento() != null) {
            criarAgendamentoAutomatico(salva, dto.dataAgendamento());
        }

        log.info("✅ {} criado com ID: {}", dto.tipoServico(), salva.getCdOrdemServico());

        // Recarregar com itens
        OrdemServico ordemComItens = ordemServicoRepository.findByIdWithItens(salva.getCdOrdemServico());
        return converterParaDTO(ordemComItens);
    }

    // ========================================
    // 2️⃣ APROVAR ORÇAMENTO (VIRA ORDEM DE SERVIÇO)
    // ========================================
    @Transactional
    public OrdemServicoResponseDTO aprovarOrcamento(Integer id, LocalDate dataAgendamento) {
        log.info("📋 Aprovando orçamento ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        // Validações
        if (ordem.getTipoServico() != TipoServico.ORCAMENTO) {
            throw new RuntimeException("❌ Apenas orçamentos podem ser aprovados");
        }

        if (ordem.getAprovado()) {
            throw new RuntimeException("❌ Orçamento já foi aprovado anteriormente");
        }

        // 🔹 SE TEM DATA → Validar disponibilidade do mecânico
        if (dataAgendamento != null) {
            validarDisponibilidadeMecanico(ordem.getMecanico().getCdUsuario(), dataAgendamento);
        }

        // 🔹 DAR BAIXA NO ESTOQUE (orçamento não dava baixa antes)
        List<ItemOrdemServico> itens = itemOrdemServicoRepository
                .findByOrdemServico_CdOrdemServico(id);

        for (ItemOrdemServico item : itens) {
            if (item.getProduto() != null) {
                Produto produto = item.getProduto();

                if (produto.getQtdEstoque() < item.getQuantidade()) {
                    throw new RuntimeException(
                            "❌ Estoque insuficiente para " + produto.getNmProduto() +
                                    ". Disponível: " + produto.getQtdEstoque() +
                                    ", Necessário: " + item.getQuantidade()
                    );
                }

                produto.setQtdEstoque(produto.getQtdEstoque() - item.getQuantidade());
                produtoRepository.save(produto);

                log.info("📦 Baixa no estoque: {} - Novo estoque: {}",
                        produto.getNmProduto(), produto.getQtdEstoque());
            }
        }

        // Converter orçamento em ordem de serviço
        ordem.setAprovado(true);
        ordem.setTipoServico(TipoServico.ORDEM_DE_SERVICO);
        ordem.setStatusOrdemServico(Status.AGUARDANDO);

        OrdemServico atualizada = ordemServicoRepository.save(ordem);

        // 🔹 SE TEM DATA → Criar agendamento
        if (dataAgendamento != null) {
            criarAgendamentoAutomatico(atualizada, dataAgendamento);
        }

        log.info("✅ Orçamento aprovado e convertido em Ordem de Serviço: {}", id);

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    // ========================================
    // 3️⃣ INICIAR ORDEM DE SERVIÇO (AGUARDANDO → EM ANDAMENTO)
    // ========================================
    @Transactional
    public OrdemServicoResponseDTO iniciar(Integer id) {
        log.info("▶️ Iniciando ordem de serviço ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getStatusOrdemServico() != Status.AGUARDANDO) {
            throw new RuntimeException("❌ Apenas ordens AGUARDANDO podem ser iniciadas");
        }

        ordem.setStatusOrdemServico(Status.EM_ANDAMENTO);
        OrdemServico atualizada = ordemServicoRepository.save(ordem);

        // 🔹 ATUALIZAR AGENDAMENTO (se existir)
        atualizarAgendamento(ordem, StatusAgendamento.EM_ANDAMENTO);

        log.info("✅ Ordem de serviço iniciada: {}", id);

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    // ========================================
    // 4️⃣ CONCLUIR ORDEM DE SERVIÇO (GERA FATURAMENTO)
    // ========================================
    @Transactional
    public OrdemServicoResponseDTO concluir(Integer id, String formaPagamento) {
        log.info("✅ Concluindo ordem de serviço ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getStatusOrdemServico() == Status.CONCLUIDA) {
            throw new RuntimeException("❌ Ordem de serviço já foi concluída");
        }

        if (ordem.getStatusOrdemServico() == Status.CANCELADA) {
            throw new RuntimeException("❌ Ordem de serviço cancelada não pode ser concluída");
        }

        // Concluir OS
        ordem.setStatusOrdemServico(Status.CONCLUIDA);
        ordem.setDataFechamento(LocalDateTime.now());

        OrdemServico concluida = ordemServicoRepository.save(ordem);

        // 🔹 GERAR FATURAMENTO AUTOMATICAMENTE
        gerarFaturamento(concluida, formaPagamento);

        // 🔹 ATUALIZAR AGENDAMENTO (se existir)
        atualizarAgendamento(ordem, StatusAgendamento.CONCLUIDO);

        log.info("✅ Ordem de serviço concluída e faturada: {}", id);

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(concluida.getCdOrdemServico()));
    }

    // ========================================
    // 5️⃣ CANCELAR ORDEM DE SERVIÇO (DEVOLVE PEÇAS)
    // ========================================
    @Transactional
    public void cancelar(Integer id) {
        log.info("🚫 Cancelando ordem de serviço ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getStatusOrdemServico() == Status.CONCLUIDA) {
            throw new RuntimeException("❌ Não é possível cancelar ordem de serviço concluída");
        }

        if (ordem.getStatusOrdemServico() == Status.CANCELADA) {
            throw new RuntimeException("❌ Ordem de serviço já está cancelada");
        }

        // 🔹 DEVOLVER PRODUTOS AO ESTOQUE (apenas se foi OS, não orçamento)
        if (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO) {
            List<ItemOrdemServico> itens = itemOrdemServicoRepository
                    .findByOrdemServico_CdOrdemServico(id);

            for (ItemOrdemServico item : itens) {
                if (item.getProduto() != null) {
                    Produto produto = item.getProduto();
                    produto.setQtdEstoque(produto.getQtdEstoque() + item.getQuantidade());
                    produtoRepository.save(produto);

                    log.info("📦 Estoque devolvido: {} - Novo estoque: {}",
                            produto.getNmProduto(), produto.getQtdEstoque());
                }
            }
        }

        // Cancelar OS
        ordem.setStatusOrdemServico(Status.CANCELADA);
        ordemServicoRepository.save(ordem);

        // 🔹 CANCELAR AGENDAMENTO (se existir)
        atualizarAgendamento(ordem, StatusAgendamento.CANCELADO);

        log.info("✅ Ordem de serviço cancelada e peças devolvidas: {}", id);
    }

    // ========================================
    // 🔧 MÉTODOS AUXILIARES
    // ========================================

    /**
     * Adiciona itens (produtos e serviços) à ordem de serviço
     * REGRA: Só dá baixa no estoque se for ORDEM_DE_SERVICO (não ORCAMENTO)
     */
    @Transactional
    private void adicionarItens(OrdemServico ordem, List<OrdemServicoDTO.ItemDTO> itensDTO) {
        double totalPecas = 0.0;
        boolean darBaixaEstoque = (ordem.getTipoServico() == TipoServico.ORDEM_DE_SERVICO);

        for (OrdemServicoDTO.ItemDTO itemDTO : itensDTO) {
            ItemOrdemServico item = new ItemOrdemServico();
            item.setOrdemServico(ordem);
            item.setQuantidade(itemDTO.quantidade());

            // Processar PRODUTO
            if (itemDTO.cdProduto() != null) {
                Produto produto = produtoRepository.findById(itemDTO.cdProduto())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.cdProduto()));

                if (!produto.getAtivo()) {
                    throw new RuntimeException("❌ Produto inativo: " + produto.getNmProduto());
                }

                // Validar estoque (sempre, mesmo para orçamento)
                if (produto.getQtdEstoque() < itemDTO.quantidade()) {
                    throw new RuntimeException(
                            "❌ Estoque insuficiente para " + produto.getNmProduto() +
                                    ". Disponível: " + produto.getQtdEstoque() +
                                    ", Solicitado: " + itemDTO.quantidade()
                    );
                }

                item.setProduto(produto);
                item.setVlUnitario(produto.getVlVenda());
                item.setVlTotal(produto.getVlVenda() * itemDTO.quantidade());
                totalPecas += item.getVlTotal();

                // 🔹 SÓ DÁ BAIXA SE FOR ORDEM DE SERVIÇO
                if (darBaixaEstoque) {
                    produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.quantidade());
                    produtoRepository.save(produto);
                    log.info("📦 Baixa no estoque: {} - Novo estoque: {}",
                            produto.getNmProduto(), produto.getQtdEstoque());
                } else {
                    log.info("📋 Orçamento: Estoque NÃO alterado para {}", produto.getNmProduto());
                }
            }

            // Processar SERVIÇO
            if (itemDTO.cdServico() != null) {
                Servico servico = servicoRepository.findById(itemDTO.cdServico())
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado: " + itemDTO.cdServico()));

                if (!servico.getAtivo()) {
                    throw new RuntimeException("❌ Serviço inativo: " + servico.getNmServico());
                }

                item.setServico(servico);
                item.setVlUnitario(servico.getVlServico());
                item.setVlTotal(servico.getVlServico() * itemDTO.quantidade());
            }

            itemOrdemServicoRepository.save(item);
        }

        // Atualizar totais
        ordem.setVlPecas(totalPecas);
        ordem.setVlTotal(ordem.getVlPecas() + ordem.getVlMaoObra() - ordem.getDesconto());
        ordemServicoRepository.save(ordem);
    }

    /**
     * Cria agendamento automaticamente quando OS tem data
     */
    @Transactional
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
        log.info("📅 Agendamento criado automaticamente para OS {} no dia {}",
                ordem.getCdOrdemServico(), dataAgendamento);
    }

    /**
     * Atualiza status do agendamento quando OS muda
     */
    @Transactional
    private void atualizarAgendamento(OrdemServico ordem, StatusAgendamento novoStatus) {
        // Buscar agendamento vinculado à OS
        List<Agendamento> agendamentos = agendamentoRepository
                .findByOrdemServico_CdOrdemServico(ordem.getCdOrdemServico());

        if (!agendamentos.isEmpty()) {
            Agendamento agendamento = agendamentos.get(0);
            agendamento.setStatus(novoStatus);
            agendamentoRepository.save(agendamento);
            log.info("📅 Agendamento atualizado para: {}", novoStatus);
        }
    }

    /**
     * Gera faturamento automaticamente ao concluir OS
     */
    @Transactional
    private void gerarFaturamento(OrdemServico ordem, String formaPagamento) {
        try {
            Faturamento faturamento = Faturamento.builder()
                    .ordemServico(ordem)
                    .dataVenda(ordem.getDataFechamento())
                    .vlTotal(ordem.getVlTotal())
                    .formaPagamento(FormaPagamento.valueOf(formaPagamento))
                    .build();

            faturamentoRepository.save(faturamento);
            log.info("💰 Faturamento gerado para OS {}: R$ {}",
                    ordem.getCdOrdemServico(), ordem.getVlTotal());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("❌ Forma de pagamento inválida: " + formaPagamento);
        }
    }

    /**
     * Valida se mecânico está disponível na data
     */
    private void validarDisponibilidadeMecanico(Integer cdMecanico, LocalDate dataAgendamento) {
        List<Agendamento> agendamentos = agendamentoRepository
                .findByMecanico_CdUsuarioAndDataAgendamentoAndStatusNot(
                        cdMecanico,
                        dataAgendamento,
                        StatusAgendamento.CANCELADO
                );

        if (!agendamentos.isEmpty()) {
            throw new RuntimeException(
                    "❌ Mecânico já possui agendamento para o dia " + dataAgendamento +
                            ". Escolha outro dia ou outro mecânico."
            );
        }
    }

    private Cliente buscarClienteAtivo(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + id));

        if (!cliente.getAtivo()) {
            throw new RuntimeException("❌ Cliente inativo não pode criar ordens de serviço");
        }

        return cliente;
    }

    private Veiculo buscarVeiculo(Integer id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado: " + id));
    }

    private Usuario buscarMecanicoAtivo(Integer id) {
        Usuario mecanico = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado: " + id));

        if (!mecanico.getAtivo()) {
            throw new RuntimeException("❌ Mecânico inativo não pode ser atribuído");
        }

        if (!mecanico.getRoles().contains(com.oficinamecanica.OficinaMecanica.enums.UserRole.ROLE_MECANICO)) {
            throw new RuntimeException("❌ Usuário não possui perfil de mecânico");
        }

        return mecanico;
    }

    // ========================================
    // 📋 CONSULTAS
    // ========================================

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorId(Integer id) {
        OrdemServico ordem = ordemServicoRepository.findByIdWithItens(id);
        if (ordem == null) {
            throw new RuntimeException("Ordem de serviço não encontrada: " + id);
        }
        return converterParaDTO(ordem);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorStatus(Status status) {
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

    @Transactional
    public OrdemServicoResponseDTO atualizar(Integer id, OrdemServicoDTO dto) {
        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada: " + id));

        if (ordem.getStatusOrdemServico() != Status.AGUARDANDO) {
            throw new RuntimeException("❌ Apenas ordens AGUARDANDO podem ser editadas");
        }

        if (dto.observacoes() != null) {
            ordem.setObservacoes(dto.observacoes());
        }

        if (dto.diagnostico() != null) {
            ordem.setDiagnostico(dto.diagnostico());
        }

        OrdemServico atualizada = ordemServicoRepository.save(ordem);
        return converterParaDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    // ========================================
    // 🔄 CONVERSORES
    // ========================================

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
        List<ItemOrdemServicoResponseDTO> itensDTO = ordem.getItens() != null
                ? ordem.getItens().stream()
                .map(this::converterItemParaDTO)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new OrdemServicoResponseDTO(
                ordem.getCdOrdemServico(),
                ordem.getCliente() != null ? ordem.getCliente().getCdCliente() : null,
                ordem.getCliente() != null ? ordem.getCliente().getNmCliente() : null,
                ordem.getVeiculo() != null ? ordem.getVeiculo().getCdVeiculo() : null,
                ordem.getVeiculo() != null ? ordem.getVeiculo().getPlaca() : null,
                ordem.getVeiculo() != null ? ordem.getVeiculo().getModelo() : null,
                ordem.getMecanico() != null ? ordem.getMecanico().getCdUsuario() : null,
                ordem.getMecanico() != null ? ordem.getMecanico().getNmUsuario() : null,
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