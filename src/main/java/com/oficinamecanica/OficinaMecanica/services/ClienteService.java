package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ClienteDTO;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de negócio de Clientes
 * Gerencia: criar, buscar, atualizar e deletar clientes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteDTO criar(ClienteDTO dto) {
        log.info("👤 Criando cliente: {}", dto.nmCliente());

        // 1. VALIDAR SE CPF JÁ EXISTE
        if (dto.cpf() != null && clienteRepository.existsByCpf(dto.cpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // 2. VALIDAR SE EMAIL JÁ EXISTE
        if (dto.email() != null && clienteRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // 3. CRIAR ENTIDADE CLIENTE
        ClienteModel cliente = ClienteModel.builder()
                .nmCliente(dto.nmCliente())
                .cpf(dto.cpf())
                .telefone(dto.telefone())
                .email(dto.email())
                .endereco(dto.endereco())
                .ativo(true)
                .build();

        // 4. SALVAR NO BANCO
        ClienteModel salvo = clienteRepository.save(cliente);

        log.info("✅ Cliente criado: ID {} - {}", salvo.getCdCliente(), salvo.getNmCliente());

        return converterParaDTO(salvo);
    }

    /**
     * BUSCAR CLIENTE POR ID
     */
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Integer id) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return converterParaDTO(cliente);
    }

    /**
     * LISTAR TODOS OS CLIENTES ATIVOS
     */
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarAtivos() {
        log.info("📋 Listando clientes ativos");

        return clienteRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR TODOS OS CLIENTES (ativos e inativos)
     */
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * BUSCAR CLIENTE POR CPF
     */
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorCpf(String cpf) {
        ClienteModel cliente = clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(cliente);
    }

    /**
     * BUSCAR CLIENTES POR NOME (busca parcial)
     */
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorNome(String nome) {
        return clienteRepository.findByNmClienteContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * ATUALIZAR CLIENTE
     */
    @Transactional
    public ClienteDTO atualizar(Integer id, ClienteDTO dto) {
        log.info("🔄 Atualizando cliente ID: {}", id);

        // 1. BUSCAR CLIENTE EXISTENTE
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2. VALIDAR CPF ÚNICO (se mudou o CPF)
        if (!cliente.getCpf().equals(dto.cpf()) &&
                clienteRepository.existsByCpf(dto.cpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // 3. VALIDAR EMAIL ÚNICO (se mudou o email)
        if (dto.email() != null &&
                !dto.email().equals(cliente.getEmail()) &&
                clienteRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // 4. ATUALIZAR CAMPOS
        cliente.setNmCliente(dto.nmCliente());
        cliente.setCpf(dto.cpf());
        cliente.setTelefone(dto.telefone());
        cliente.setEmail(dto.email());
        cliente.setEndereco(dto.endereco());

        // 5. SALVAR E RETORNAR
        ClienteModel atualizado = clienteRepository.save(cliente);

        log.info("✅ Cliente atualizado: {}", atualizado.getNmCliente());

        return converterParaDTO(atualizado);
    }

    /**
     * DELETAR CLIENTE (SOFT DELETE)
     */
    @Transactional
    public void deletar(Integer id) {
        log.info("🗑️ Deletando cliente ID: {}", id);

        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        cliente.setAtivo(false);
        clienteRepository.save(cliente);

        log.info("✅ Cliente marcado como inativo");
    }

    /**
     * CONVERTER MODEL PARA DTO
     */
    private ClienteDTO converterParaDTO(ClienteModel cliente) {
        return new ClienteDTO(
                cliente.getCdCliente(),
                cliente.getNmCliente(),
                cliente.getCpf(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getAtivo()
        );
    }
}