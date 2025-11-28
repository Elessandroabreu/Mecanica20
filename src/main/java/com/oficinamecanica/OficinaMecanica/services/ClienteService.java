package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ClienteDTO;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteDTO criar(ClienteDTO dto) {
        // Validar CPF único
        if (dto.CPF() != null && clienteRepository.existsByNuCPF(dto.CPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // Validar email único
        if (dto.email() != null && clienteRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        ClienteModel cliente = ClienteModel.builder()
                .nmCliente(dto.nmCliente())
                .CPF(dto.CPF())
                .Telefone(dto.Telefone())
                .Endereco(dto.Endereco())
                .email(dto.email())
                .ativo(true)
                .build();

        ClienteModel salvo = clienteRepository.save(cliente);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Integer id) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarAtivos() {
        return clienteRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorCpf(String cpf) {
        ClienteModel cliente = clienteRepository.findByNuCPF(cpf)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorNome(String nome) {
        return clienteRepository.findByNmClienteContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO atualizar(Integer id, ClienteDTO dto) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Validar CPF único (se mudou)
        if (!cliente.getCPF().equals(dto.CPF()) && clienteRepository.existsByNuCPF(dto.CPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // Validar email único (se mudou)
        if (dto.email() != null && !dto.email().equals(cliente.getEmail()) &&
                clienteRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        cliente.setNmCliente(dto.nmCliente());
        cliente.setCPF(dto.CPF());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.Telefone());
        cliente.setEndereco(dto.Endereco());

        ClienteModel atualizado = clienteRepository.save(cliente);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Soft delete
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

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