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
        log.info("Criando {} para cliente: {}", dto.tipoOrdemOrcamento(), dto.cdCliente());

        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAtivo())
            throw new RuntimeException("Cliente inativo");

        VeiculoModel veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        UsuarioModel mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        if (!mecanico.getAtivo())
            throw new RuntimeException("Mecânico inativo");

        if (dto.tipoOrdemOrcamento() == TipoOrdemOrcamento.ORDEM_DE_SERVICO &&
                dto.dataAgendamento() != null) {
            validarDisponibilidadeMecanico(dto.cdMecanico(), dto.dataAgendamento());
        }

        Status statusInicial = (dto.tipoOrdemOrcamento() == TipoOrdemOrcamento.ORCAMENTO)
                ? Status.ORCAMENTO
                : Status.AGENDADO;

        OrdemServicoModel ordem = OrdemServicoModel.builder()
                .clienteModel(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .tipoOrdemOrcamento(dto.tipoOrdemOrcamento())
                .status(statusInicial) // ← CORRIGIDO
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

        OrdemServicoModel salva = ordemServicoRepository.save(ordem);

        if (dto.itens() != null && !dto.itens().isEmpty())
            adicionarItens(salva, dto.itens());

        if (dto.tipoOrdemOrcamento() == TipoOrdemOrcamento.ORDEM_DE_SERVICO &&
                dto.dataAgendamento() != null) {
            criarAgendamentoAutomatico(salva, dto.dataAgendamento());
        }

        return converterParaResponseDTO(ordemServicoRepository.findByIdWithItens(salva.getCdOrdemServico()));
    }

    @Transactional
    protected void adicionarItens(OrdemServicoModel ordem, List<OrdemServicoRequestDTO.ItemDTO> itensDTO) {
        double totalPecas = 0.0;
        double totalServicos = 0.0;

        boolean darBaixaEstoque = ordem.getTipoOrdemOrcamento() == TipoOrdemOrcamento.ORDEM_DE_SERVICO;

        for (OrdemServicoRequestDTO.ItemDTO itemDTO : itensDTO) {
            ItemOrdemServicoModel item = new ItemOrdemServicoModel();
            item.setOrdemServico(ordem);
            item.setQuantidade(itemDTO.quantidade());

            if (itemDTO.cdProduto() != null) {
                ProdutoModel produto = produtoRepository.findById(itemDTO.cdProduto())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

                if (!produto.getAtivo())
                    throw new RuntimeException("Produto inativo: " + produto.getNmProduto());

                if (produto.getQtdEstoque() < itemDTO.quantidade())
                    throw new RuntimeException("Estoque insuficiente para " + produto.getNmProduto());

                item.setProduto(produto);
                item.setVlUnitario(produto.getVlProduto());
                item.setVlTotal(produto.getVlProduto() * itemDTO.quantidade());
                totalPecas += item.getVlTotal();

                if (darBaixaEstoque) {
                    produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.quantidade());
                    produtoRepository.save(produto);
                }
            }

            if (itemDTO.cdServico() != null) {
                ServicoModel servico = servicoRepository.findById(itemDTO.cdServico())
                        .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

                if (!servico.getAtivo())
                    throw new RuntimeException("Serviço inativo");

                item.setServico(servico);
                item.setVlUnitario(servico.getVlServico());
                item.setVlTotal(servico.getVlServico() * itemDTO.quantidade());
                totalServicos += item.getVlTotal();
            }

            itemOrdemServicoRepository.save(item);
        }

        ordem.setVlPecas(totalPecas);
        ordem.setVlServicos(totalServicos);
        ordem.setVlTotal(totalPecas + totalServicos + ordem.getVlMaoObraExtra());
        ordemServicoRepository.save(ordem);
    }

    @Transactional
    public OrdemServicoResponseDTO aprovarOrcamento(Integer id, LocalDate dataAgendamento) {
        log.info("📋 Aprovando orçamento ID: {}", id);

        OrdemServicoModel ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getTipoOrdemOrcamento() != TipoOrdemOrcamento.ORCAMENTO)
            throw new RuntimeException("Apenas orçamentos podem ser aprovados");

        if (ordem.getAprovado())
            throw new RuntimeException("Orçamento já aprovado");

        if (dataAgendamento != null)
            validarDisponibilidadeMecanico(ordem.getMecanico().getCdUsuario(), dataAgendamento);

        for (ItemOrdemServicoModel item : itemOrdemServicoRepository.findByOrdemServico_CdOrdemServico(id)) {
            if (item.getProduto() != null) {
                ProdutoModel produto = item.getProduto();

                if (produto.getQtdEstoque() < item.getQuantidade())
                    throw new RuntimeException("Estoque insuficiente para " + produto.getNmProduto());

                produto.setQtdEstoque(produto.getQtdEstoque() - item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        ordem.setAprovado(true);
        ordem.setTipoOrdemOrcamento(TipoOrdemOrcamento.ORDEM_DE_SERVICO);
        ordem.setStatus(Status.AGENDADO);

        if (dataAgendamento != null)
            ordem.setDataAgendamento(dataAgendamento.atStartOfDay());

        OrdemServicoModel atualizada = ordemServicoRepository.save(ordem);

        if (dataAgendamento != null)
            criarAgendamentoAutomatico(atualizada, dataAgendamento);

        return converterParaResponseDTO(
                ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico())
        );
    }

    @Transactional
    public OrdemServicoResponseDTO iniciar(Integer id) {

        OrdemServicoModel ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() != Status.AGENDADO)
            throw new RuntimeException("Apenas ordens AGENDADAS podem ser iniciadas");

        ordem.setStatus(Status.EM_ANDAMENTO);
        OrdemServicoModel atualizada = ordemServicoRepository.save(ordem);

        atualizarAgendamento(ordem, Status.EM_ANDAMENTO);

        return converterParaResponseDTO(ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    @Transactional
    public OrdemServicoResponseDTO concluir(Integer id, String formaPagamento) {

        OrdemServicoModel ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() == Status.CONCLUIDO)
            throw new RuntimeException("Ordem já concluída");

        if (ordem.getStatus() == Status.CANCELADO)
            throw new RuntimeException("Ordem cancelada não pode ser concluída");

        ordem.setStatus(Status.CONCLUIDO);
        OrdemServicoModel concluida = ordemServicoRepository.save(ordem);

        gerarFaturamento(concluida, formaPagamento);
        atualizarAgendamento(ordem, Status.CONCLUIDO);

        return converterParaResponseDTO(
                ordemServicoRepository.findByIdWithItens(concluida.getCdOrdemServico())
        );
    }

    @Transactional
    public void cancelar(Integer id) {

        OrdemServicoModel ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() == Status.CONCLUIDO)
            throw new RuntimeException("Ordem concluída não pode ser cancelada");

        if (ordem.getStatus() == Status.CANCELADO)
            throw new RuntimeException("Ordem já cancelada");

        if (ordem.getTipoOrdemOrcamento() == TipoOrdemOrcamento.ORDEM_DE_SERVICO) {
            for (ItemOrdemServicoModel item : itemOrdemServicoRepository
                    .findByOrdemServico_CdOrdemServico(id)) {

                if (item.getProduto() != null) {
                    ProdutoModel produto = item.getProduto();
                    produto.setQtdEstoque(produto.getQtdEstoque() + item.getQuantidade());
                    produtoRepository.save(produto);
                }
            }
        }

        ordem.setStatus(Status.CANCELADO);
        ordemServicoRepository.save(ordem);

        atualizarAgendamento(ordem, Status.CANCELADO);
    }

    @Transactional
    protected void criarAgendamentoAutomatico(OrdemServicoModel ordem, LocalDate dataAgendamento) {

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
    }

    @Transactional
    protected void atualizarAgendamento(OrdemServicoModel ordem, Status novoStatus) {
        List<AgendamentoModel> agendamentos =
                agendamentoRepository.findByOrdemServico_CdOrdemServico(ordem.getCdOrdemServico());

        if (!agendamentos.isEmpty()) {
            AgendamentoModel agendamento = agendamentos.get(0);
            agendamento.setStatus(novoStatus);
            agendamentoRepository.save(agendamento);
        }
    }

    @Transactional
    protected void gerarFaturamento(OrdemServicoModel ordem, String formaPagamento) {

        FaturamentoModel faturamento = FaturamentoModel.builder()
                .ordemServico(ordem)
                .dataVenda(LocalDateTime.now())
                .vlTotal(ordem.getVlTotal())
                .formaPagamento(FormaPagamento.valueOf(formaPagamento))
                .build();

        faturamentoRepository.save(faturamento);
    }

    private void validarDisponibilidadeMecanico(Integer cdMecanico, LocalDate dataAgendamento) {
        if (!agendamentoRepository
                .findByMecanico_CdUsuarioAndDataAgendamentoAndStatusNot(
                        cdMecanico, dataAgendamento, Status.CANCELADO
                ).isEmpty()) {

            throw new RuntimeException("Mecânico já tem agendamento para " + dataAgendamento);
        }
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorId(Integer id) {
        OrdemServicoModel ordem = ordemServicoRepository.findByIdWithItens(id);
        if (ordem == null)
            throw new RuntimeException("Ordem não encontrada");

        return converterParaResponseDTO(ordem);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorStatus(Status status) {
        return ordemServicoRepository.findByStatus(status).stream()
                .map(ordem -> converterParaResponseDTO(
                        ordemServicoRepository.findByIdWithItens(ordem.getCdOrdemServico())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarOrcamentosPendentes() {
        return ordemServicoRepository.findOrcamentosPendentes().stream()
                .map(ordem -> converterParaResponseDTO(
                        ordemServicoRepository.findByIdWithItens(ordem.getCdOrdemServico())
                ))
                .toList();
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarDiagnosticoEMaoObra(
            Integer cdOrdemServico,
            String diagnostico,
            Double vlMaoObraExtra
    ) {
        log.info("🔄 Atualizando dados da ordem #{}", cdOrdemServico);


        OrdemServicoModel ordem = ordemServicoRepository.findById(cdOrdemServico)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));


        ordem.setDiagnostico(diagnostico);
        ordem.setVlMaoObraExtra(vlMaoObraExtra != null ? vlMaoObraExtra : 0.0);

        List<ItemOrdemServicoModel> itens =
                itemOrdemServicoRepository.findByOrdemServico_CdOrdemServico(cdOrdemServico);

        double totalPecas = itens.stream()
                .filter(i -> i.getProduto() != null)
                .mapToDouble(ItemOrdemServicoModel::getVlTotal)
                .sum();

        double totalServicos = itens.stream()
                .filter(i -> i.getServico() != null)
                .mapToDouble(ItemOrdemServicoModel::getVlTotal)
                .sum();

        double novoTotal = totalPecas + totalServicos + ordem.getVlMaoObraExtra();

        ordem.setVlPecas(totalPecas);
        ordem.setVlServicos(totalServicos);
        ordem.setVlTotal(novoTotal);

        OrdemServicoModel salva = ordemServicoRepository.save(ordem);

        if (salva.getStatus() == Status.CONCLUIDO) {

            FaturamentoModel faturamento =
                    faturamentoRepository.findByOrdemServico_CdOrdemServico(cdOrdemServico);

            if (faturamento != null) {
                faturamento.setVlTotal(novoTotal);
                faturamento.setDataVenda(LocalDateTime.now());
                faturamentoRepository.save(faturamento);

                log.info("💰 Faturamento atualizado automaticamente!");
            }
        }

        return converterParaResponseDTO(salva);
    }

    @Transactional
    public OrdemServicoResponseDTO atualizar(Integer id, OrdemServicoRequestDTO dto) {

        OrdemServicoModel ordem = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem não encontrada"));

        if (ordem.getStatus() != Status.AGENDADO)
            throw new RuntimeException("Apenas ordens AGENDADAS podem ser editadas");

        if (dto.diagnostico() != null)
            ordem.setDiagnostico(dto.diagnostico());

        if (dto.vlMaoObra() != null) {
            ordem.setVlMaoObraExtra(dto.vlMaoObra());
            ordem.setVlTotal(ordem.getVlPecas() + ordem.getVlServicos() + ordem.getVlMaoObraExtra());
        }

        OrdemServicoModel atualizada = ordemServicoRepository.save(ordem);

        return converterParaResponseDTO(
                ordemServicoRepository.findByIdWithItens(atualizada.getCdOrdemServico()));
    }

    private OrdemServicoResponseDTO converterParaResponseDTO(OrdemServicoModel ordem) {

        List<OrdemServicoResponseDTO.ItemResponseDTO> itensDTO = ordem.getItens() != null
                ? ordem.getItens().stream().map(item -> new OrdemServicoResponseDTO.ItemResponseDTO(
                item.getCdItemOrdemServico(),
                item.getProduto() != null ? item.getProduto().getCdProduto() : null,
                item.getProduto() != null ? item.getProduto().getNmProduto() : null,
                item.getServico() != null ? item.getServico().getCdServico() : null,
                item.getServico() != null ? item.getServico().getNmServico() : null,
                item.getQuantidade(),
                item.getVlUnitario(),
                item.getVlTotal()
        )).toList() : new ArrayList<>();

        return new OrdemServicoResponseDTO(
                ordem.getCdOrdemServico(),
                ordem.getClienteModel().getCdCliente(),
                ordem.getClienteModel().getNmCliente(),
                ordem.getVeiculo().getCdVeiculo(),
                ordem.getVeiculo().getPlaca(),
                ordem.getVeiculo().getModelo(),
                ordem.getVeiculo().getMarca(),
                ordem.getMecanico().getCdUsuario(),
                ordem.getMecanico().getNmUsuario(),
                ordem.getTipoOrdemOrcamento(),
                ordem.getStatus(),
                ordem.getDataAgendamento(),
                ordem.getDataAbertura(),
                ordem.getVlPecas(),
                ordem.getVlServicos(),
                ordem.getVlMaoObraExtra(),
                ordem.getVlTotal(),
                ordem.getDiagnostico(),
                ordem.getAprovado(),
                itensDTO
        );
    }

    public List<OrdemServicoResponseDTO> listarTodas() {
        return ordemServicoRepository.findAllWithBasicRelations().stream()
                .map(ordem -> new OrdemServicoResponseDTO(
                        ordem.getCdOrdemServico(),
                        ordem.getClienteModel().getCdCliente(),
                        ordem.getClienteModel().getNmCliente(),
                        ordem.getVeiculo().getCdVeiculo(),
                        ordem.getVeiculo().getPlaca(),
                        ordem.getVeiculo().getModelo(),
                        ordem.getVeiculo().getMarca(),
                        ordem.getMecanico().getCdUsuario(),
                        ordem.getMecanico().getNmUsuario(),
                        ordem.getTipoOrdemOrcamento(),
                        ordem.getStatus(),
                        ordem.getDataAgendamento(),
                        ordem.getDataAbertura(),
                        ordem.getVlPecas(),
                        ordem.getVlServicos(),
                        ordem.getVlMaoObraExtra(),
                        ordem.getVlTotal(),
                        ordem.getDiagnostico(),
                        ordem.getAprovado(),
                        null
                ))
                .collect(Collectors.toList());
    }
}
