package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ServicoDTO;
import com.oficinamecanica.OficinaMecanica.models.ServicoModel;
import com.oficinamecanica.OficinaMecanica.repositories.ServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de negócio de Serviços
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional
    public ServicoDTO criar(ServicoDTO dto) {
        log.info("Criando serviço: {}", dto.nmServico());

        ServicoModel servico = ServicoModel.builder()
                .nmServico(dto.nmServico())
                .dsServico(dto.dsServico())
                .vlServico(dto.vlServico())
                .ativo(true)
                .build();

        ServicoModel salvo = servicoRepository.save(servico);

        log.info("Serviço criado: ID {} - {}", salvo.getCdServico(), salvo.getNmServico());

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ServicoDTO buscarPorId(Integer id) {
        ServicoModel servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        return converterParaDTO(servico);
    }

    @Transactional(readOnly = true)
    public List<ServicoDTO> listarAtivos() {
        log.info("📋 Listando serviços ativos");

        return servicoRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServicoDTO> listarTodos() {
        return servicoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServicoDTO> buscarPorNome(String nome) {
        return servicoRepository.findByNmServicoContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicoDTO atualizar(Integer id, ServicoDTO dto) {
        log.info("Atualizando serviço ID: {}", id);

        ServicoModel servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        servico.setNmServico(dto.nmServico());
        servico.setDsServico(dto.dsServico());
        servico.setVlServico(dto.vlServico());

        ServicoModel atualizado = servicoRepository.save(servico);

        log.info("Serviço atualizado: {}", atualizado.getNmServico());

        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        log.info("Deletando serviço ID: {}", id);

        ServicoModel servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        servico.setAtivo(false);
        servicoRepository.save(servico);

        log.info("Serviço marcado como inativo");
    }

    private ServicoDTO converterParaDTO(ServicoModel servico) {
        return new ServicoDTO(
                servico.getCdServico(),
                servico.getNmServico(),
                servico.getDsServico(),
                servico.getVlServico(),
                servico.getAtivo()
        );
    }
}