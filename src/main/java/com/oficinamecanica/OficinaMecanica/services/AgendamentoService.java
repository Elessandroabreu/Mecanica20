package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.AgendamentoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.AgendamentoResponseDTO;
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

    // ✅ CRIAR NOVO AGENDAMENTO - USA RequestDTO e RETORNA ResponseDTO
    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto) {
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
                .dataAgendamento(dto.dataAgendamento())
                .observacoes(dto.observacoes())
                .status(Status.AGENDADO) // ✅ Status inicial sempre AGENDADO
                .build();

        AgendamentoModel salvo = agendamentoRepository.save(agendamento);

        log.info("✅ Agendamento criado com ID: {}", salvo.getCdAgendamento());

        return converterParaResponseDTO(salvo);
    }

    // ✅ ATUALIZAR STATUS
    @Transactional
    public AgendamentoResponseDTO atualizarStatus(Integer id, Status novoStatus) {
        log.info("🔄 Atualizando status do agendamento {} para: {}", id, novoStatus);

        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        Status statusAntigo = agendamento.getStatus();
        agendamento.setStatus(novoStatus);

        AgendamentoModel atualizado = agendamentoRepository.save(agendamento);

        // Sincronizar com OS se existir
        sincronizarComOrdemServico(agendamento, novoStatus);

        log.info("✅ Status alterado: {} → {}", statusAntigo, novoStatus);

        return converterParaResponseDTO(atualizado);
    }

    // ✅ SINCRONIZAR COM ORDEM DE SERVIÇO
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

    // ✅ BUSCAR POR ID
    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Integer id) {
        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return converterParaResponseDTO(agendamento);
    }

    // ✅ LISTAR TODOS
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarTodos() {
        log.info("📋 Listando todos os agendamentos");

        List<AgendamentoModel> agendamentos = agendamentoRepository.findAll();

        return agendamentos.stream()
                .map(this::converterParaResponseDTO)
                .sorted((a, b) -> b.dataAgendamento().compareTo(a.dataAgendamento()))
                .collect(Collectors.toList());
    }

    // ✅ LISTAR POR MECÂNICO
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorMecanico(Integer cdMecanico) {
        return agendamentoRepository.findByMecanico_CdUsuario(cdMecanico).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ LISTAR FUTUROS
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAgendamentosFuturos() {
        return agendamentoRepository.findAgendamentosFuturos(LocalDate.now()).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ ATUALIZAR - USA RequestDTO e RETORNA ResponseDTO
    @Transactional
    public AgendamentoResponseDTO atualizar(Integer id, AgendamentoRequestDTO dto) {
        log.info("🔄 Atualizando agendamento ID: {}", id);

        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Atualizar campos
        agendamento.setDataAgendamento(dto.dataAgendamento());
        agendamento.setObservacoes(dto.observacoes());

        // Validar disponibilidade se mudou a data
        if (!agendamento.getDataAgendamento().equals(dto.dataAgendamento())) {
            validarDisponibilidadeMecanico(
                    agendamento.getMecanico().getCdUsuario(),
                    dto.dataAgendamento()
            );
        }

        AgendamentoModel atualizado = agendamentoRepository.save(agendamento);

        log.info("✅ Agendamento atualizado: ID {}", id);

        return converterParaResponseDTO(atualizado);
    }

    // ✅ CANCELAR
    @Transactional
    public void cancelar(Integer id) {
        log.info("🚫 Cancelando agendamento ID: {}", id);
        atualizarStatus(id, Status.CANCELADO);
    }

    // ✅ VALIDAR DISPONIBILIDADE DO MECÂNICO
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

    // ✅ CONVERTER PARA ResponseDTO - VERSÃO COMPLETA CORRIGIDA
    private AgendamentoResponseDTO converterParaResponseDTO(AgendamentoModel agendamento) {
        return new AgendamentoResponseDTO(
                // ID do agendamento
                agendamento.getCdAgendamento(),

                // Dados do Cliente
                agendamento.getCdCliente().getCdCliente(),
                agendamento.getCdCliente().getNmCliente(),
                agendamento.getCdCliente().getCpf(),
                agendamento.getCdCliente().getTelefone(),

                // Dados do Veículo
                agendamento.getVeiculo().getCdVeiculo(),
                agendamento.getVeiculo().getPlaca(),
                agendamento.getVeiculo().getModelo(),
                agendamento.getVeiculo().getMarca(),

                // Dados do Mecânico
                agendamento.getMecanico().getCdUsuario(),
                agendamento.getMecanico().getNmUsuario(),

                // Dados do Agendamento
                agendamento.getDataAgendamento(),
                agendamento.getStatus(),
                agendamento.getObservacoes(),

                // Ordem de Serviço vinculada (pode ser null)
                agendamento.getOrdemServico() != null ?
                        agendamento.getOrdemServico().getCdOrdemServico() : null
        );
    }
}