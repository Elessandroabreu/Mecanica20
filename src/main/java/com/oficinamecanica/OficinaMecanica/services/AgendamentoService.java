package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.AgendamentoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.AgendamentoResponseDTO;
import com.oficinamecanica.OficinaMecanica.models.Agendamento;
import com.oficinamecanica.OficinaMecanica.models.Cliente;
import com.oficinamecanica.OficinaMecanica.models.Usuario;
import com.oficinamecanica.OficinaMecanica.models.Veiculo;
import com.oficinamecanica.OficinaMecanica.repositories.AgendamentoRepository;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import com.oficinamecanica.OficinaMecanica.repositories.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getCdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Veiculo veiculo = veiculoRepository.findById(dto.getCdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        Usuario mecanico = usuarioRepository.findById(dto.getCdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        // Validar disponibilidade do mecânico
        if (agendamentoRepository.existsAgendamentoNoHorario(dto.getCdMecanico(), dto.getHorario())) {
            throw new RuntimeException("Mecânico já possui agendamento neste horário");
        }

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .horario(dto.getHorario())
                .observacoes(dto.getObservacoes())
                .status(Agendamento.StatusAgendamento.AGENDADO)
                .dataAgendamento(LocalDateTime.now())
                .build();

        Agendamento salvo = agendamentoRepository.save(agendamento);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Integer id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return converterParaDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorMecanico(Integer cdMecanico) {
        return agendamentoRepository.findByMecanico_CdUsuario(cdMecanico).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAgendamentosFuturos() {
        return agendamentoRepository.findAgendamentosFuturos(LocalDateTime.now()).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgendamentoResponseDTO atualizar(Integer id, AgendamentoRequestDTO dto) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Validar disponibilidade se horário foi alterado
        if (!agendamento.getHorario().equals(dto.getHorario())) {
            if (agendamentoRepository.existsAgendamentoNoHorario(dto.getCdMecanico(), dto.getHorario())) {
                throw new RuntimeException("Mecânico já possui agendamento neste horário");
            }
            agendamento.setHorario(dto.getHorario());
        }

        agendamento.setObservacoes(dto.getObservacoes());
        agendamento.setStatus(dto.getStatus());

        Agendamento atualizado = agendamentoRepository.save(agendamento);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void cancelar(Integer id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        agendamento.setStatus(Agendamento.StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
    }

    private AgendamentoResponseDTO converterParaDTO(Agendamento agendamento) {
        return AgendamentoResponseDTO.builder()
                .cdAgendamento(agendamento.getCdAgendamento())
                .cdCliente(agendamento.getCliente().getCdCliente())
                .nmCliente(agendamento.getCliente().getNmCliente())
                .cdVeiculo(agendamento.getVeiculo().getCdVeiculo())
                .placa(agendamento.getVeiculo().getPlaca())
                .cdMecanico(agendamento.getMecanico().getCdUsuario())
                .nmMecanico(agendamento.getMecanico().getNmUsuario())
                .horario(agendamento.getHorario())
                .status(agendamento.getStatus())
                .observacoes(agendamento.getObservacoes())
                .dataAgendamento(agendamento.getDataAgendamento())
                .build();
    }
}
