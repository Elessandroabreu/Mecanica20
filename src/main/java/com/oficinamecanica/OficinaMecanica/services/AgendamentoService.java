package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.AgendamentoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.AgendamentoResponseDTO;
import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
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

        Cliente cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Veiculo veiculo = veiculoRepository.findById(dto.cdVeiculo())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        Usuario mecanico = usuarioRepository.findById(dto.cdMecanico())
                .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .mecanico(mecanico)
                .observacoes(dto.observacoes())
                .status(dto.status() != null ? dto.status() : StatusAgendamento.AGENDADO)
                .dataAgendamento(dto.dataAgendamento().atStartOfDay())
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

        agendamento.setObservacoes(dto.observacoes());
        agendamento.setStatus(dto.status());
        agendamento.setDataAgendamento(dto.dataAgendamento().atStartOfDay());  // atualiza a data do agendamento

        Agendamento atualizado = agendamentoRepository.save(agendamento);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void cancelar(Integer id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
    }

    private AgendamentoResponseDTO converterParaDTO(Agendamento agendamento) {
        return new AgendamentoResponseDTO(
                agendamento.getCdAgendamento(),
                agendamento.getCliente().getCdCliente(),
                agendamento.getCliente().getNmCliente(),
                agendamento.getVeiculo().getCdVeiculo(),
                agendamento.getVeiculo().getPlaca(),
                agendamento.getMecanico().getCdUsuario(),
                agendamento.getMecanico().getNmUsuario(),
                agendamento.getStatus(),
                agendamento.getObservacoes(),
                agendamento.getDataAgendamento()
        );
    }
}
