package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ClienteDTO;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de negócio de Clientes
 * Gerencia: criar, buscar, atualizar e deletar clientes
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    // Injeção do Repository para acessar o banco de dados
    private final ClienteRepository clienteRepository;

    /**
     * CRIAR NOVO CLIENTE
     * Valida CPF e email únicos antes de salvar
     */
    @Transactional
    public ClienteDTO criar(ClienteDTO dto) {
        // 1. VALIDAR SE CPF JÁ EXISTE
        if (dto.CPF() != null && clienteRepository.existsByNuCPF(dto.CPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // 2. VALIDAR SE EMAIL JÁ EXISTE
        if (dto.email() != null && clienteRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // 3. CRIAR ENTIDADE CLIENTE
        // Atenção: os campos do banco são com MAIÚSCULAS!
        ClienteModel cliente = ClienteModel.builder()
                .nmCliente(dto.nmCliente())
                .CPF(dto.CPF())              // Campo do banco: CPF
                .Telefone(dto.Telefone())    // Campo do banco: Telefone
                .Endereco(dto.Endereco())    // Campo do banco: Endereco
                .email(dto.email())
                .ativo(true)                 // Novo cliente sempre começa ativo
                .build();

        // 4. SALVAR NO BANCO
        ClienteModel salvo = clienteRepository.save(cliente);

        // 5. CONVERTER PARA DTO E RETORNAR
        return converterParaDTO(salvo);
    }

    /**
     * BUSCAR CLIENTE POR ID
     */
    @Transactional(readOnly = true) // Só leitura
    public ClienteDTO buscarPorId(Integer id) {
        // Busca no banco, se não achar lança erro
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return converterParaDTO(cliente);
    }

    /**
     * LISTAR TODOS OS CLIENTES ATIVOS
     * Retorna apenas clientes com ativo = true
     */
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarAtivos() {
        // Busca todos os clientes ativos
        return clienteRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO) // Converte cada um para DTO
                .collect(Collectors.toList()); // Junta tudo em uma lista
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
        ClienteModel cliente = clienteRepository.findByNuCPF(cpf)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(cliente);
    }

    /**
     * BUSCAR CLIENTES POR NOME (busca parcial)
     * Exemplo: buscar "João" retorna "João Silva", "Maria João", etc
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
        // 1. BUSCAR CLIENTE EXISTENTE
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2. VALIDAR CPF ÚNICO (se mudou o CPF)
        if (!cliente.getCPF().equals(dto.CPF()) &&
                clienteRepository.existsByNuCPF(dto.CPF())) {
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
        cliente.setCPF(dto.CPF());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.Telefone());
        cliente.setEndereco(dto.Endereco());

        // 5. SALVAR E RETORNAR
        ClienteModel atualizado = clienteRepository.save(cliente);
        return converterParaDTO(atualizado);
    }

    /**
     * DELETAR CLIENTE (SOFT DELETE)
     * Não remove do banco, apenas marca como inativo
     */
    @Transactional
    public void deletar(Integer id) {
        // Buscar cliente
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Marcar como inativo (soft delete)
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    // ========== MÉTODO AUXILIAR ==========

    /**
     * CONVERTER MODEL PARA DTO
     * Transforma a entidade do banco em DTO
     */
    private ClienteDTO converterParaDTO(ClienteModel cliente) {
        return new ClienteDTO(
                cliente.getCdCliente(),
                cliente.getNmCliente(),
                cliente.getCPF(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getEmail(),
                cliente.getAtivo()
        );
    }
}