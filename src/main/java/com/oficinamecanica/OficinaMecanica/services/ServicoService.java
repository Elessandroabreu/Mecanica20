package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.ServicoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.ServicoResponseDTO;
import com.oficinamecanica.OficinaMecanica.models.Servico;
import com.oficinamecanica.OficinaMecanica.repositories.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional
    public ServicoResponseDTO criar(ServicoRequestDTO dto) {
        Servico servico = Servico.builder()
                .nmServico(dto.getNmServico())
                .dsServico(dto.getDsServico())
                .vlServico(dto.getVlServico())
                .tmpEstimado(dto.getTmpEstimado())
                .ativo(true)
                .build();

        Servico salvo = servicoRepository.save(servico);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO buscarPorId(Integer id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        return converterParaDTO(servico);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarAtivos() {
        return servicoRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicoResponseDTO atualizar(Integer id, ServicoRequestDTO dto) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        servico.setNmServico(dto.getNmServico());
        servico.setDsServico(dto.getDsServico());
        servico.setVlServico(dto.getVlServico());
        servico.setTmpEstimado(dto.getTmpEstimado());

        Servico atualizado = servicoRepository.save(servico);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    private ServicoResponseDTO converterParaDTO(Servico servico) {
        return ServicoResponseDTO.builder()
                .cdServico(servico.getCdServico())
                .nmServico(servico.getNmServico())
                .dsServico(servico.getDsServico())
                .vlServico(servico.getVlServico())
                .tmpEstimado(servico.getTmpEstimado())
                .ativo(servico.getAtivo())
                .dataCadastro(servico.getDataCadastro())
                .build();
    }
}
