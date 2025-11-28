package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.AgendamentoDTO;
import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.AgendamentoModel;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import com.oficinamecanica.OficinaMecanica.models.OrdemServico;
import com.oficinamecanica.OficinaMecanica.models.Usuario;
import com.oficinamecanica.OficinaMecanica.models.Veiculo;
import com.oficinamecanica.OficinaMecanica.repositories.AgendamentoRepository;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import com.oficinamecanica.OficinaMecanica.repositories.OrdemServicoRepository;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import com.oficinamecanica.OficinaMecanica.repositories.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar Agendamentos
 *
 * SINCRONIZAÇÃO COM ORDEM DE SERVIÇO:
 * - Quando agendamento muda para EM_ANDAMENTO → OS muda para EM_ANDAMENTO
 * - Quando agendamento muda para CONCLUIDO → OS muda para CONCLUIDA
 * - Quando agendamento é CANCELADO → OS muda para CANCELADA
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    // ========================================
    // 1️⃣ CRIAR AGENDAMENTO
    // ========================================
    @Transactional
    public AgendamentoDTO criar(AgendamentoDTO dto) {
        log.info("📅 Criando agendamento para cliente: {}", dto.cdCliente());

        ClienteModel clienteModel = buscarClienteAtivo(dto.cdCliente());
        Veiculo veiculo = buscarVeiculo(dto.cdVeiculo());
        Usuario mecanico = buscarMecanicoAtivo(dto.cdMecanico());

        // Validar disponibilidade do mecânico
        validarDisponibilidadeMecanico(dto.cdMecanico(), dto.dataAgendamento());

        AgendamentoModel agendamentoModel = AgendamentoModel.builder()
                .clienteModel(clienteModel)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .observacoes(dto.observacoes())
                .status(dto.status() != null ? dto.status() : Status.AGENDADO)
                .dataAgendamento(dto.dataAgendamento())
                .build();

        AgendamentoModel salvo = agendamentoRepository.save(agendamentoModel);
        log.info("✅ Agendamento criado com ID: {}", salvo.getCdAgendamento());

        return converterParaDTO(salvo);
    }

    // ========================================
    // 2️⃣ ATUALIZAR STATUS DO AGENDAMENTO
    // ========================================
    @Transactional
    public AgendamentoDTO atualizarStatus(Integer id, Status novoStatus) {
        log.info("🔄 Atualizando status do agendamento {} para: {}", id, novoStatus);

        AgendamentoModel agendamentoModel = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        Status statusAntigo = agendamentoModel.getStatus();
        agendamentoModel.setStatus(novoStatus);

        AgendamentoModel atualizado = agendamentoRepository.save(agendamentoModel);

        // 🔹 SINCRONIZAR COM ORDEM DE SERVIÇO (se existir)
        sincronizarComOrdemServico(agendamentoModel, novoStatus);

        log.info("✅ Status do agendamento alterado: {} → {}", statusAntigo, novoStatus);

        return converterParaDTO(atualizado);
    }

    // ========================================
    // 3️⃣ SINCRONIZAÇÃO BIDIRECIONAL
    // ========================================

    /**
     * Sincroniza mudanças do Agendamento com a Ordem de Serviço vinculada
     *
     * MAPEAMENTO:
     * - AGENDADO → AGUARDANDO
     * - EM_ANDAMENTO → EM_ANDAMENTO
     * - CONCLUIDO → CONCLUIDA
     * - CANCELADO → CANCELADA
     */
    @Transactional
    protected void sincronizarComOrdemServico(AgendamentoModel agendamentoModel, Status novoStatus) {
        if (agendamentoModel.getOrdemServico() == null) {
            log.info("ℹ️ Agendamento não possui OS vinculada");
            return;
        }

        OrdemServico os = agendamentoModel.getOrdemServico();
        Status novoStatusOS = mapearStatusParaOS(novoStatus);

        if (novoStatusOS != null && os.getStatus() != novoStatusOS) {
            os.setStatus(novoStatusOS);
            ordemServicoRepository.save(os);

            log.info("🔗 Ordem de Serviço {} sincronizada: {}",
                    os.getCdOrdemServico(), novoStatusOS);
        }
    }

    /**
     * Mapeia status do Agendamento para status da Ordem de Serviço
     */
    private Status mapearStatusParaOS(Status status) {
        return switch (status) {
            case AGENDADO -> Status.AGENDADO;
            case EM_ANDAMENTO -> Status.EM_ANDAMENTO;
            case CONCLUIDO -> Status.CONCLUIDO;
            case CANCELADO -> Status.CANCELADO;
        };
    }

    // ========================================
    // 🔧 MÉTODOS DE CONSULTA
    // ========================================

    @Transactional(readOnly = true)
    public AgendamentoDTO buscarPorId(Integer id) {
        AgendamentoModel agendamentoModel = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return converterParaDTO(agendamentoModel);
    }

    // ✅ NOVO: Listar TODOS os agendamentos (incluindo os criados automaticamente)
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarTodos() {
        log.info("📋 Listando todos os agendamentos");
        List<AgendamentoModel> agendamentoModels = agendamentoRepository.findAll();
        log.info("✅ Total de agendamentos encontrados: {}", agendamentoModels.size());

        return agendamentoModels.stream()
                .map(this::converterParaDTO)
                .sorted((a, b) -> b.dataAgendamento().compareTo(a.dataAgendamento())) // Mais recentes primeiro
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarPorMecanico(Integer cdMecanico) {
        return agendamentoRepository.findByMecanico_CdUsuario(cdMecanico).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarAgendamentosFuturos() {
        return agendamentoRepository.findAgendamentosFuturos(LocalDate.now()).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgendamentoDTO atualizar(Integer id, AgendamentoDTO dto) {
        AgendamentoModel agendamentoModel = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        agendamentoModel.setObservacoes(dto.observacoes());

        if (dto.status() != null && dto.status() != agendamentoModel.getStatus()) {
            return atualizarStatus(id, dto.status());
        }

        agendamentoModel.setDataAgendamento(dto.dataAgendamento());

        AgendamentoModel atualizado = agendamentoRepository.save(agendamentoModel);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void cancelar(Integer id) {
        atualizarStatus(id, Status.CANCELADO);
    }


    private void validarDisponibilidadeMecanico(Integer cdMecanico, LocalDate dataAgendamento) {
        List<AgendamentoModel> agendamentoModels = agendamentoRepository
                .findByMecanico_CdUsuarioAndDataAgendamentoAndStatusNot(
                        cdMecanico,
                        dataAgendamento,
                        Status.CANCELADO
                );

        if (!agendamentoModels.isEmpty()) {
            throw new RuntimeException(
                    "❌ Mecânico já possui agendamento para o dia " + dataAgendamento +
                            ". Escolha outro dia ou outro mecânico."
            );
        }
    }

    private ClienteModel buscarClienteAtivo(Integer id) {
        ClienteModel clienteModel = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!clienteModel.getAtivo()) {
            throw new RuntimeException("❌ Cliente inativo não pode criar agendamentos");
        }

        return clienteModel;
    }

    private Veiculo buscarVeiculo(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        // Validar se veículo pertence ao cliente
        if (veiculo.getClienteModel() == null) {
            throw new RuntimeException("❌ Veículo sem cliente associado");
        }

        return veiculo;
    }

    private Usuario buscarMecanicoAtivo(Integer id) {
        Usuario mecanico = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        if (!mecanico.getAtivo()) {
            throw new RuntimeException("❌ Mecânico inativo não pode ser atribuído");
        }

        if (!mecanico.getRoles().contains(UserRole.ROLE_MECANICO)) {
            throw new RuntimeException("❌ Usuário não possui perfil de mecânico");
        }

        return mecanico;
    }

    private AgendamentoDTO converterParaDTO(AgendamentoModel agendamentoModel) {
        return new AgendamentoDTO(
                agendamentoModel.getCdAgendamento(),
                agendamentoModel.getClienteModel().getCdCliente(),
                agendamentoModel.getClienteModel().getNmCliente(),
                agendamentoModel.getVeiculo().getCdVeiculo(),
                agendamentoModel.getVeiculo().getPlaca(),
                agendamentoModel.getMecanico().getCdUsuario(),
                agendamentoModel.getMecanico().getNmUsuario(),
                agendamentoModel.getStatus(),
                agendamentoModel.getObservacoes(),
                agendamentoModel.getDataAgendamento()
        );
    }
}