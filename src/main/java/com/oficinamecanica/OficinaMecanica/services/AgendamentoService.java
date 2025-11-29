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

    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto) {
        log.info("📅 Criando agendamento para cliente: {}", dto.cdCliente());

        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAtivo()) {
            throw new RuntimeException("Cliente inativo não pode criar agendamentos");
        }

        VeiculoModel veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        UsuarioModel mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        if (!mecanico.getAtivo()) {
            throw new RuntimeException("Mecânico inativo");
        }

        if (!mecanico.getRoles().contains(UserRole.ROLE_MECANICO)) {
            throw new RuntimeException("Usuário não é mecânico");
        }

        validarDisponibilidadeMecanico(dto.cdMecanico(), dto.dataAgendamento());

        AgendamentoModel agendamento = AgendamentoModel.builder()
                .cdCliente(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .dataAgendamento(dto.dataAgendamento())
                .observacoes(dto.observacoes())
                .status(Status.AGENDADO) // ✅ Status inicial sempre AGENDADO
                .build();

        AgendamentoModel salvo = agendamentoRepository.save(agendamento);

        log.info("Agendamento criado com ID: {}", salvo.getCdAgendamento());

        return converterParaResponseDTO(salvo);
    }

    @Transactional
    public AgendamentoResponseDTO atualizarStatus(Integer id, Status novoStatus) {
        log.info("Atualizando status do agendamento {} para: {}", id, novoStatus);

        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        Status statusAntigo = agendamento.getStatus();
        agendamento.setStatus(novoStatus);

        AgendamentoModel atualizado = agendamentoRepository.save(agendamento);

        sincronizarComOrdemServico(agendamento, novoStatus);

        log.info("Status alterado: {} → {}", statusAntigo, novoStatus);

        return converterParaResponseDTO(atualizado);
    }

    @Transactional
    protected void sincronizarComOrdemServico(AgendamentoModel agendamento, Status novoStatus) {
        if (agendamento.getOrdemServico() == null) {
            return;
        }

        OrdemServicoModel os = agendamento.getOrdemServico();

        if (os.getStatus() != novoStatus) {
            os.setStatus(novoStatus);
            ordemServicoRepository.save(os);
            log.info("🔗 OS {} sincronizada: {}", os.getCdOrdemServico(), novoStatus);
        }
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Integer id) {
        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return converterParaResponseDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarTodos() {
        log.info("📋 Listando todos os agendamentos");

        List<AgendamentoModel> agendamentos = agendamentoRepository.findAll();

        return agendamentos.stream()
                .map(this::converterParaResponseDTO)
                .sorted((a, b) -> b.dataAgendamento().compareTo(a.dataAgendamento()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorMecanico(Integer cdMecanico) {
        return agendamentoRepository.findByMecanico_CdUsuario(cdMecanico).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAgendamentosFuturos() {
        return agendamentoRepository.findAgendamentosFuturos(LocalDate.now()).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgendamentoResponseDTO atualizar(Integer id, AgendamentoRequestDTO dto) {
        log.info("🔄 Atualizando agendamento ID: {}", id);

        AgendamentoModel agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));


        agendamento.setDataAgendamento(dto.dataAgendamento());
        agendamento.setObservacoes(dto.observacoes());

        if (!agendamento.getDataAgendamento().equals(dto.dataAgendamento())) {
            validarDisponibilidadeMecanico(
                    agendamento.getMecanico().getCdUsuario(),
                    dto.dataAgendamento()
            );
        }

        AgendamentoModel atualizado = agendamentoRepository.save(agendamento);

        log.info("Agendamento atualizado: ID {}", id);

        return converterParaResponseDTO(atualizado);
    }

    @Transactional
    public void cancelar(Integer id) {
        log.info("🚫 Cancelando agendamento ID: {}", id);
        atualizarStatus(id, Status.CANCELADO);
    }

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

    private AgendamentoResponseDTO converterParaResponseDTO(AgendamentoModel agendamento) {
        return new AgendamentoResponseDTO(
                // ID do agendamento
                agendamento.getCdAgendamento(),

                agendamento.getCdCliente().getCdCliente(),
                agendamento.getCdCliente().getNmCliente(),
                agendamento.getCdCliente().getCpf(),
                agendamento.getCdCliente().getTelefone(),

                agendamento.getVeiculo().getCdVeiculo(),
                agendamento.getVeiculo().getPlaca(),
                agendamento.getVeiculo().getModelo(),
                agendamento.getVeiculo().getMarca(),

                agendamento.getMecanico().getCdUsuario(),
                agendamento.getMecanico().getNmUsuario(),

                agendamento.getDataAgendamento(),
                agendamento.getStatus(),
                agendamento.getObservacoes(),

                agendamento.getOrdemServico() != null ?
                        agendamento.getOrdemServico().getCdOrdemServico() : null
        );
    }
}