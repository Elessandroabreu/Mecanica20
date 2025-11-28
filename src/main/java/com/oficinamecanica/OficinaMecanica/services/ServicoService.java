package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ServicoDTO;
import com.oficinamecanica.OficinaMecanica.models.Servico;
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

    /**
     * CRIAR NOVO TIPO DE SERVIÇO
     */
    @Transactional
    public ServicoDTO criar(ServicoDTO dto) {
        log.info("🔧 Criando serviço: {}", dto.nmServico());

        Servico servico = Servico.builder()
                .nmServico(dto.nmServico())
                .dsServico(dto.dsServico())
                .vlServico(dto.vlServico())
                .ativo(true)
                .build();

        Servico salvo = servicoRepository.save(servico);

        log.info("✅ Serviço criado: ID {} - {}", salvo.getCdServico(), salvo.getNmServico());

        return converterParaDTO(salvo);
    }

    /**
     * BUSCAR SERVIÇO POR ID
     */
    @Transactional(readOnly = true)
    public ServicoDTO buscarPorId(Integer id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        return converterParaDTO(servico);
    }

    /**
     * LISTAR SERVIÇOS ATIVOS
     */
    @Transactional(readOnly = true)
    public List<ServicoDTO> listarAtivos() {
        log.info("📋 Listando serviços ativos");

        return servicoRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR TODOS OS SERVIÇOS
     */
    @Transactional(readOnly = true)
    public List<ServicoDTO> listarTodos() {
        return servicoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * BUSCAR SERVIÇOS POR NOME
     */
    @Transactional(readOnly = true)
    public List<ServicoDTO> buscarPorNome(String nome) {
        return servicoRepository.findByNmServicoContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * ATUALIZAR SERVIÇO
     */
    @Transactional
    public ServicoDTO atualizar(Integer id, ServicoDTO dto) {
        log.info("🔄 Atualizando serviço ID: {}", id);

        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        servico.setNmServico(dto.nmServico());
        servico.setDsServico(dto.dsServico());
        servico.setVlServico(dto.vlServico());

        Servico atualizado = servicoRepository.save(servico);

        log.info("✅ Serviço atualizado: {}", atualizado.getNmServico());

        return converterParaDTO(atualizado);
    }

    /**
     * DELETAR SERVIÇO (SOFT DELETE)
     */
    @Transactional
    public void deletar(Integer id) {
        log.info("🗑️ Deletando serviço ID: {}", id);

        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        servico.setAtivo(false);
        servicoRepository.save(servico);

        log.info("✅ Serviço marcado como inativo");
    }

    /**
     * CONVERTER MODEL PARA DTO
     */
    private ServicoDTO converterParaDTO(Servico servico) {
        return new ServicoDTO(
                servico.getCdServico(),
                servico.getNmServico(),
                servico.getDsServico(),
                servico.getVlServico(),
                servico.getAtivo()
        );
    }
}