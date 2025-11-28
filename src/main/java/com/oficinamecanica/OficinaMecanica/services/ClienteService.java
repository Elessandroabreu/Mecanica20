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
        // ✅ VALIDAÇÃO: CPF já existe?
        if (dto.nuCPF() != null && clienteRepository.existsByNuCPF(dto.nuCPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // ✅ CRIAR ENTIDADE com nomes corretos dos campos
        ClienteModel clienteModel = ClienteModel.builder()
                .nmCliente(dto.nmCliente())
                .CPF(dto.nuCPF())           // ✅ CORRIGIDO
                .Telefone(dto.nuTelefone()) // ✅ CORRIGIDO
                .Endereco(dto.dsEndereco()) // ✅ CORRIGIDO
                .email(dto.email())
                .ativo(true)
                .build();

        ClienteModel salvo = clienteRepository.save(clienteModel);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Integer id) {
        ClienteModel clienteModel = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(clienteModel);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarAtivos() {
        return clienteRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO atualizar(Integer id, ClienteDTO dto) {
        ClienteModel clienteModel = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // ✅ ATUALIZAR com nomes corretos
        clienteModel.setNmCliente(dto.nmCliente());
        clienteModel.setCPF(dto.nuCPF());           // ✅ CORRIGIDO
        clienteModel.setTelefone(dto.nuTelefone()); // ✅ CORRIGIDO
        clienteModel.setEndereco(dto.dsEndereco()); // ✅ CORRIGIDO
        clienteModel.setEmail(dto.email());

        ClienteModel atualizado = clienteRepository.save(clienteModel);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        ClienteModel clienteModel = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        clienteModel.setAtivo(false);
        clienteRepository.save(clienteModel);
    }

    // ✅ CONVERTER ENTIDADE → DTO
    private ClienteDTO converterParaDTO(ClienteModel clienteModel) {
        return new ClienteDTO(
                clienteModel.getCdCliente(),
                clienteModel.getNmCliente(),
                clienteModel.getCPF(),
                clienteModel.getTelefone(),
                clienteModel.getEndereco(),
                clienteModel.getEmail(),
                clienteModel.getAtivo()
        );
    }
}