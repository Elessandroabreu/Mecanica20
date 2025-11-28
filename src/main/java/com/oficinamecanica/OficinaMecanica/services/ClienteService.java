package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.ClienteDTO;
import com.oficinamecanica.OficinaMecanica.models.Cliente;
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
        // ✅ VALIDAÇÃO: CPF já existe?
        if (dto.nuCPF() != null && clienteRepository.existsByNuCPF(dto.nuCPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // ✅ CRIAR ENTIDADE com nomes corretos dos campos
        Cliente cliente = Cliente.builder()
                .nmCliente(dto.nmCliente())
                .CPF(dto.nuCPF())           // ✅ CORRIGIDO
                .Telefone(dto.nuTelefone()) // ✅ CORRIGIDO
                .Endereco(dto.dsEndereco()) // ✅ CORRIGIDO
                .email(dto.email())
                .ativo(true)
                .build();

        Cliente salvo = clienteRepository.save(cliente);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarAtivos() {
        return clienteRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO atualizar(Integer id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // ✅ ATUALIZAR com nomes corretos
        cliente.setNmCliente(dto.nmCliente());
        cliente.setCPF(dto.nuCPF());           // ✅ CORRIGIDO
        cliente.setTelefone(dto.nuTelefone()); // ✅ CORRIGIDO
        cliente.setEndereco(dto.dsEndereco()); // ✅ CORRIGIDO
        cliente.setEmail(dto.email());

        Cliente atualizado = clienteRepository.save(cliente);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    // ✅ CONVERTER ENTIDADE → DTO
    private ClienteDTO converterParaDTO(Cliente cliente) {
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