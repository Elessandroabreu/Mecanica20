package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.OrdemServicoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.OrdemServicoResponseDTO;
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

    // CRIAR ORDEM OU ORÇAMENTO
    @Transactional
    public OrdemServicoRequestDTO criar(OrdemServicoRequestDTO dto) {
        log.info("🆕 Criando {} para cliente: {}", dto.tipoServico(), dto.cdCliente());

        // Buscar entidades
        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAtivo()) {
            throw new RuntimeException("Cliente inativo");
        }

        Veiculo veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        Usuario mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        if (!mecanico.getAtivo()) {
            throw new RuntimeException("Mecânico inativo");
        }

        // Validar disponibilidade se for ORDEM_DE_SERVICO com data
        if (dto.tipoServico() == TipoOrdemOrcamento.ORDEM_DE_SERVICO && dto.dataAgendamento() != null) {
            validarDisponibilidadeMecanico(dto.cdMecanico(), dto.dataAgendamento());
        }

        // Criar ordem
        OrdemServico ordem = OrdemServico.builder()
                .clienteModel(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .tipoOrdemOrcamento(dto.tipoServico())
                .status(Status.AGENDADO)
                .dataAbertura(LocalDateTime.now())
                .dataAgendamento(dto.dataAgendamento() != null ?
                        dto.dataAgendamento().atStartOfDay() : LocalDateTime.now())
                .vlPecas(0.0)
                .vlServicos(0.0)
                .vlMaoObraExtra(dto.vlMaoObra() != null ? dto.vlMaoObra() : 0.0)
                .vlTotal(0.0)
                .diagnostico(dto.diagnostico())
                .aprovado(false)
                .itens(new ArrayList<>())
                .build();

        OrdemServico salva = ordemServicoRepository.save(ordem);

        // Adicionar itens
        if (dto.itens() != null && !dto.itens().isEmpty()) {
            adicionarItens(salva, dto.itens());
        }

        // Criar agendamento se ORDEM_DE_SERVICO com data
        if (dto.tipoServico() == TipoOrdemOrcamento.ORDEM_DE_SERVICO && dto.dataAgendamento() != null) {
            criarAgendamentoAutomatico(salva, dto.dataAgendamento());
        }

        log.info("✅ {} criado ID: {}", dto.tipoServico(), salva.getCdOrdemServico());

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(salva.getCdOrdemServico()));
    }

    // ADICIONAR ITENS
    @Transactional
    private void adicionarItens(OrdemServico ordem, List<OrdemServicoRequestDTO.ItemDTO> itensDTO) {
        double totalPecas = 0.0;
        double totalServicos = 0.0;

        // Só dá baixa se for ORDEM_DE_SERVICO
        boolean darBaixaEstoque = (ordem.getTipoOrdemOrcamento() == TipoOrdemOrcamento.ORDEM_DE_SERVICO);

        for (OrdemServicoRequestDTO.ItemDTO itemDTO : itensDTO) {
            ItemOrdemServico item = new ItemOrdemServico();
            item.setOrdemServico(ordem);
            item.setQuantidade(itemDTO.quantidade());

            // PRODUTO
            if (itemDTO.cdProduto() != null) {
                Produto produto = produtoRepository.findById(itemDTO.cdProduto())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

                if (!produto.getAtivo()) {
                    throw new RuntimeException("Produto inativo: " + produto.getNmProduto());
                }

                if (produto.getQtdEstoque() < itemDTO.quantidade()) {
                    throw new RuntimeException(
                            "Estoque insuficiente para " + produto.getNmProduto() +
                                    ". Disponível: " + produto.getQtdEstoque()
                    );
                }

                item.setProduto(produto);
                item.setVlUnitario(produto.getVlProduto());
                item.setVlTotal(produto.getVlProduto() * itemDTO.quantidade());
                totalPecas += item.getVlTotal();

                // Dar baixa apenas se ORDEM_DE_SERVICO
                if (darBaixaEstoque) {
                    produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.quantidade());
                    produtoRepository.save(produto);
                    log.info("📦 Baixa: {} - Estoque: {}", produto.getNmProduto(), produto.getQtdEstoque());
                }
            }

            // SERVIÇO
            if (itemDTO.cdServico() != null) {
                Servico servico = servicoRepository.findById(itemDTO.cdServico())
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

                if (!servico.getAtivo()) {
                    throw new RuntimeException("Serviço inativo");
                }

                item.setServico(servico);
                item.setVlUnitario(servico.getVlServico());
                item.setVlTotal(servico.getVlServico() * itemDTO.quantidade());
                totalServicos += item.getVlTotal();
            }

            itemOrdemServicoRepository.save(item);
        }

        // Atualizar totais
        ordem.setVlPecas(totalPecas);
        ordem.setVlServicos(totalServicos);
        ordem.setVlTotal(totalPecas + totalServicos + ordem.getVlMaoObraExtra());
        ordemServicoRepository.save(ordem);
    }

    // APROVAR ORÇAMENTO
    @Transactional
    public OrdemServicoRequestDTO aprovarOrcamento(Integer id, LocalDate dataAgendamento) {
        log.info("📋 Aprovando orçamento ID: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getTipoOrdemOrcamento() != TipoOrdemOrcamento.ORCAMENTO) {
            throw new RuntimeException("Apenas orçamentos podem ser aprovados");
        }

        if (ordem.getAprovado()) {
            throw new RuntimeException("Orçamento já aprovado");
        }

        // Validar disponibilidade
        if (dataAgendamento != null) {
            validarDisponibilidadeMecanico(ordem.getMecanico().getCdUsuario(), dataAgendamento);
        }

        // DAR BAIXA NO ESTOQUE (orçamento não dava baixa)
        List<ItemOrdemServico> itens = itemOrdemServicoRepository
                .findByOrdemServico_CdOrdemServico(id);

        for (ItemOrdemServico item : itens) {
            if (item.getProduto() != null) {
                Produto produto = item.getProduto();

                if (produto.getQtdEstoque() < item.getQuantidade()) {
                    throw new RuntimeException(
                            "Estoque insuficiente para " + produto.getNmProduto()
                    );
                }

                produto.setQtdEstoque(produto.getQtdEstoque() - item.getQuantidade());
                produtoRepository.save(produto);
                log.info("📦 Baixa: {}", produto.getNmProduto());
            }
        }

        // Converter para ORDEM_DE_SERVICO
        ordem.setAprovado(true);
        ordem.setTipoOrdemOrcamento(TipoOrdemOrcamento.ORDEM_DE_SERVICO);
        ordem.setStatus(Status.AGENDADO);

        if (dataAgendamento != null) {
            ordem.setDataAgendamento(dataAgendamento.atStartOfDay());
        }

        OrdemServico atualizada = ordemServicoRepository.save(ordem);

        // Criar agendamento
        if (dataAgendamento != null) {
            criarAgendamentoAutomatico(atualizada, dataAgendamento);
        }

        log.info("✅ Orçamento aprovado: {}", id);

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    // INICIAR
    @Transactional
    public OrdemServicoRequestDTO iniciar(Integer id) {
        log.info("▶️ Iniciando OS: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() != Status.AGENDADO) {
            throw new RuntimeException("Apenas ordens AGENDADAS podem ser iniciadas");
        }

        ordem.setStatus(Status.EM_ANDAMENTO);
        OrdemServico atualizada = ordemServicoRepository.save(ordem);

        atualizarAgendamento(ordem, Status.EM_ANDAMENTO);

        log.info("✅ OS iniciada: {}", id);

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    // CONCLUIR
    @Transactional
    public OrdemServicoRequestDTO concluir(Integer id, String formaPagamento) {
        log.info("✅ Concluindo OS: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() == Status.CONCLUIDO) {
            throw new RuntimeException("Ordem já concluída");
        }

        if (ordem.getStatus() == Status.CANCELADO) {
            throw new RuntimeException("Ordem cancelada não pode ser concluída");
        }

        ordem.setStatus(Status.CONCLUIDO);
        OrdemServico concluida = ordemServicoRepository.save(ordem);

        // Gerar faturamento
        gerarFaturamento(concluida, formaPagamento);

        // Atualizar agendamento
        atualizarAgendamento(ordem, Status.CONCLUIDO);

        log.info("✅ OS concluída: {}", id);

        return converterParaDTO(ordemServicoRepository.findByIdWithItens(concluida.getCdOrdemServico()));
    }

    // CANCELAR
    @Transactional
    public void cancelar(Integer id) {
        log.info("🚫 Cancelando OS: {}", id);

        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() == Status.CONCLUIDO) {
            throw new RuntimeException("Ordem concluída não pode ser cancelada");
        }

        if (ordem.getStatus() == Status.CANCELADO) {
            throw new RuntimeException("Ordem já cancelada");
        }

        // DEVOLVER ESTOQUE (apenas se ORDEM_DE_SERVICO)
        if (ordem.getTipoOrdemOrcamento() == TipoOrdemOrcamento.ORDEM_DE_SERVICO) {
            List<ItemOrdemServico> itens = itemOrdemServicoRepository
                    .findByOrdemServico_CdOrdemServico(id);

            for (ItemOrdemServico item : itens) {
                if (item.getProduto() != null) {
                    Produto produto = item.getProduto();
                    produto.setQtdEstoque(produto.getQtdEstoque() + item.getQuantidade());
                    produtoRepository.save(produto);
                    log.info("📦 Devolvido: {}", produto.getNmProduto());
                }
            }
        }

        ordem.setStatus(Status.CANCELADO);
        ordemServicoRepository.save(ordem);

        atualizarAgendamento(ordem, Status.CANCELADO);

        log.info("✅ OS cancelada: {}", id);
    }

    // CRIAR AGENDAMENTO AUTOMÁTICO
    @Transactional
    private void criarAgendamentoAutomatico(OrdemServico ordem, LocalDate dataAgendamento) {
        AgendamentoModel agendamento = AgendamentoModel.builder()
                .cdCliente(ordem.getClienteModel())
                .veiculo(ordem.getVeiculo())
                .mecanico(ordem.getMecanico())
                .dataAgendamento(dataAgendamento)
                .status(Status.AGENDADO)
                .observacoes("Agendamento da OS #" + ordem.getCdOrdemServico())
                .ordemServico(ordem)
                .build();

        agendamentoRepository.save(agendamento);
        log.info("📅 Agendamento criado para OS {}", ordem.getCdOrdemServico());
    }

    // ATUALIZAR AGENDAMENTO
    @Transactional
    private void atualizarAgendamento(OrdemServico ordem, Status novoStatus) {
        List<AgendamentoModel> agendamentos = agendamentoRepository
                .findByOrdemServico_CdOrdemServico(ordem.getCdOrdemServico());

        if (!agendamentos.isEmpty()) {
            AgendamentoModel agendamento = agendamentos.get(0);
            agendamento.setStatus(novoStatus);
            agendamentoRepository.save(agendamento);
            log.info("📅 Agendamento atualizado: {}", novoStatus);
        }
    }

    // GERAR FATURAMENTO
    @Transactional
    private void gerarFaturamento(OrdemServico ordem, String formaPagamento) {
        try {
            Faturamento faturamento = Faturamento.builder()
                    .ordemServico(ordem)
                    .dataVenda(LocalDateTime.now())
                    .vlTotal(ordem.getVlTotal())
                    .formaPagamento(FormaPagamento.valueOf(formaPagamento))
                    .build();

            faturamentoRepository.save(faturamento);
            log.info("💰 Faturamento gerado: R$ {}", ordem.getVlTotal());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Forma de pagamento inválida: " + formaPagamento);
        }
    }

    // VALIDAR DISPONIBILIDADE
    private void validarDisponibilidadeMecanico(Integer cdMecanico, LocalDate dataAgendamento) {
        List<AgendamentoModel> agendamentos = agendamentoRepository
                .findByMecanico_CdUsuarioAndDataAgendamentoAndStatusNot(
                        cdMecanico,
                        dataAgendamento,
                        Status.CANCELADO
                );

        if (!agendamentos.isEmpty()) {
            throw new RuntimeException("Mecânico já tem agendamento para " + dataAgendamento);
        }
    }

    // BUSCAR POR ID
    @Transactional(readOnly = true)
    public OrdemServicoRequestDTO buscarPorId(Integer id) {
        OrdemServico ordem = ordemServicoRepository.findByIdWithItens(id);
        if (ordem == null) {
            throw new RuntimeException("Ordem não encontrada");
        }
        return converterParaDTO(ordem);
    }

    // LISTAR POR STATUS
    @Transactional(readOnly = true)
    public List<OrdemServicoRequestDTO> listarPorStatus(Status status) {
        return ordemServicoRepository.findByStatus(status).stream()
                .map(ordem -> converterParaDTO(
                        ordemServicoRepository.findByIdWithItens(ordem.getCdOrdemServico())
                ))
                .toList();
    }

    // LISTAR ORÇAMENTOS PENDENTES
    @Transactional(readOnly = true)
    public List<OrdemServicoRequestDTO> listarOrcamentosPendentes() {
        return ordemServicoRepository.findOrcamentosPendentes().stream()
                .map(ordem -> converterParaDTO(
                        ordemServicoRepository.findByIdWithItens(ordem.getCdOrdemServico())
                ))
                .toList();
    }

    // ATUALIZAR
    @Transactional
    public OrdemServicoRequestDTO atualizar(Integer id, OrdemServicoRequestDTO dto) {
        OrdemServico ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() != Status.AGENDADO) {
            throw new RuntimeException("Apenas ordens AGENDADAS podem ser editadas");
        }

        // ✅ REMOVIDO: ordem.setObservacoes() - campo não existe no Model
        // Apenas atualizar diagnóstico
        if (dto.diagnostico() != null) {
            ordem.setDiagnostico(dto.diagnostico());
        }

        // ✅ NOVO: Atualizar vlMaoObraExtra se fornecido
        if (dto.vlMaoObra() != null) {
            ordem.setVlMaoObraExtra(dto.vlMaoObra());
            // Recalcular total
            ordem.setVlTotal(ordem.getVlPecas() + ordem.getVlServicos() + ordem.getVlMaoObraExtra());
        }

        OrdemServico atualizada = ordemServicoRepository.save(ordem);
        return converterParaDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    private OrdemServicoResponseDTO converterParaDTO(OrdemServico ordem) {
        // Converter itens
        List<OrdemServicoResponseDTO.ItemResponseDTO> itensDTO = ordem.getItens() != null
                ? ordem.getItens().stream()
                .map(item -> new OrdemServicoResponseDTO.ItemResponseDTO(
                        item.getCdItemOrdemServico(),
                        item.getProduto() != null ? item.getProduto().getCdProduto() : null,
                        item.getProduto() != null ? item.getProduto().getNmProduto() : null,
                        item.getServico() != null ? item.getServico().getCdServico() : null,
                        item.getServico() != null ? item.getServico().getNmServico() : null,
                        item.getQuantidade(),
                        item.getVlUnitario(),
                        item.getVlTotal()
                ))
                .toList()
                : new ArrayList<>();

        return new OrdemServicoResponseDTO(
                ordem.getCdOrdemServico(),
                // Cliente
                ordem.getClienteModel().getCdCliente(),
                ordem.getClienteModel().getNmCliente(),
                // Veículo
                ordem.getVeiculo().getCdVeiculo(),
                ordem.getVeiculo().getPlaca(),
                ordem.getVeiculo().getModelo(),
                ordem.getVeiculo().getMarca(),
                // Mecânico
                ordem.getMecanico().getCdUsuario(),
                ordem.getMecanico().getNmUsuario(),
                // Dados da OS
                ordem.getTipoOrdemOrcamento(),
                ordem.getStatus(),
                ordem.getDataAgendamento(),
                ordem.getDataAbertura(),
                // Valores
                ordem.getVlPecas(),
                ordem.getVlServicos(),
                ordem.getVlMaoObraExtra(),
                ordem.getVlTotal(),
                ordem.getDiagnostico(),
                ordem.getAprovado(),
                itensDTO
        );
    }
}