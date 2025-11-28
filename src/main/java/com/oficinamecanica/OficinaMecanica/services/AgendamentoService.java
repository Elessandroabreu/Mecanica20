package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.AgendamentoDTO;
import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.*;
import com.oficinamecanica.OficinaMecanica.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    // CRIAR NOVO AGENDAMENTO
    @Transactional
    public AgendamentoDTO criar(AgendamentoDTO dto) {
        log.info("📅 Criando agendamento para cliente: {}", dto.cdCliente());

        // Buscar cliente
        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAtivo()) {
            throw new RuntimeException("Cliente inativo não pode criar agendamentos");
        }

        // Buscar veículo
        Veiculo veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        // Buscar mecânico
        Usuario mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        if (!mecanico.getAtivo()) {
            throw new RuntimeException("Mecânico inativo");
        }

        if (!mecanico.getRoles().contains(UserRole.ROLE_MECANICO)) {
            throw new RuntimeException("Usuário não é mecânico");
        }

        // Validar disponibilidade
        validarDisponibilidadeMecanico(dto.cdMecanico(), dto.dataAgendamento());

        // Criar agendamento
        AgendamentoModel agendamento = AgendamentoModel.builder()
                .cdCliente(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .observacoes(dto.observacoes())
                .status(dto.status() != null ? dto.status() : Status.AGENDADO)
                .dataAgendamento(dto.dataAgendamento())
                .build();

        AgendamentoModel salvo = agendamentoRepository.save(agendamento);

        log.info("✅ Agendamento criado com ID: {}", salvo.getCdAgendamento());

        return converterParaDTO(salvo);
    }

    // ATUALIZAR STATUS
    @Transactional
    public AgendamentoDTO atualizarStatus(Integer id, Status novoStatus) {
        log.info("🔄 Atualizando status do agendamento {} para: {}", id, novoStatus);

        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        Status statusAntigo = agendamento.getStatus();
        agendamento.setStatus(novoStatus);

        AgendamentoModel atualizado = agendamentoRepository.save(agendamento);

        // Sincronizar com OS se existir
        sincronizarComOrdemServico(agendamento, novoStatus);

        log.info("✅ Status alterado: {} → {}", statusAntigo, novoStatus);

        return converterParaDTO(atualizado);
    }

    // SINCRONIZAR COM ORDEM DE SERVIÇO
    @Transactional
    protected void sincronizarComOrdemServico(AgendamentoModel agendamento, Status novoStatus) {
        if (agendamento.getOrdemServico() == null) {
            return;
        }

        OrdemServico os = agendamento.getOrdemServico();

        if (os.getStatus() != novoStatus) {
            os.setStatus(novoStatus);
            ordemServicoRepository.save(os);
            log.info("🔗 OS {} sincronizada: {}", os.getCdOrdemServico(), novoStatus);
        }
    }

    // BUSCAR POR ID
    @Transactional(readOnly = true)
    public AgendamentoDTO buscarPorId(Integer id) {
        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return converterParaDTO(agendamento);
    }

    // LISTAR TODOS
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarTodos() {
        log.info("📋 Listando todos os agendamentos");

        List<AgendamentoModel> agendamentos = agendamentoRepository.findAll();

        return agendamentos.stream()
                .map(this::converterParaDTO)
                .sorted((a, b) -> b.dataAgendamento().compareTo(a.dataAgendamento()))
                .collect(Collectors.toList());
    }

    // LISTAR POR MECÂNICO
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarPorMecanico(Integer cdMecanico) {
        return agendamentoRepository.findByMecanico_CdUsuario(cdMecanico).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    // LISTAR FUTUROS
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarAgendamentosFuturos() {
        return agendamentoRepository.findAgendamentosFuturos(LocalDate.now()).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    // ATUALIZAR
    @Transactional
    public AgendamentoDTO atualizar(Integer id, AgendamentoDTO dto) {
        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        agendamento.setObservacoes(dto.observacoes());

        if (dto.status() != null && dto.status() != agendamento.getStatus()) {
            return atualizarStatus(id, dto.status());
        }

        agendamento.setDataAgendamento(dto.dataAgendamento());

        AgendamentoModel atualizado = agendamentoRepository.save(agendamento);
        return converterParaDTO(atualizado);
    }

    // CANCELAR
    @Transactional
    public void cancelar(Integer id) {
        atualizarStatus(id, Status.CANCELADO);
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
            throw new RuntimeException(
                    "Mecânico já tem agendamento para " + dataAgendamento
            );
        }
    }

    // CONVERTER PARA DTO
    private AgendamentoDTO converterParaDTO(AgendamentoModel agendamento) {
        return new AgendamentoDTO(
                agendamento.getCdCliente().getCdCliente(),
                agendamento.getVeiculo().getCdVeiculo(),
                agendamento.getMecanico().getCdUsuario(),
                agendamento.getDataAgendamento(),
                agendamento.getObservacoes(),
                agendamento.getStatus()
        );
    }
}
