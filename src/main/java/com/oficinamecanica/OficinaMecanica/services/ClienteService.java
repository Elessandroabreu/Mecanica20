package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.ClienteRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.ClienteResponseDTO;
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
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        if (dto.nuCPF() != null && clienteRepository.existsByNuCPF(dto.nuCPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        Cliente cliente = Cliente.builder()
                .nmCliente(dto.nmCliente())
                .nuCPF(dto.nuCPF())
                .nuTelefone(dto.nuTelefone())
                .dsEndereco(dto.dsEndereco())
                .email(dto.email())
                .ativo(true)
                .build();

        Cliente salvo = clienteRepository.save(cliente);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return converterParaDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarAtivos() {
        return clienteRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponseDTO atualizar(Integer id, ClienteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        cliente.setNmCliente(dto.nmCliente());
        cliente.setNuCPF(dto.nuCPF());
        cliente.setNuTelefone(dto.nuTelefone());
        cliente.setDsEndereco(dto.dsEndereco());
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

    private ClienteResponseDTO converterParaDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getCdCliente(),
                cliente.getNmCliente(),
                cliente.getNuCPF(),
                cliente.getNuTelefone(),
                cliente.getDsEndereco(),
                cliente.getEmail(),
                cliente.getAtivo()
        );
    }
}