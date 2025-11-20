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
        if (dto.getNuCPF() != null && clienteRepository.existsByNuCPF(dto.getNuCPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        Cliente cliente = Cliente.builder()
                .nmCliente(dto.getNmCliente())
                .nuCPF(dto.getNuCPF())
                .nuTelefone(dto.getNuTelefone())
                .dsEndereco(dto.getDsEndereco())
                .email(dto.getEmail())
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

        cliente.setNmCliente(dto.getNmCliente());
        cliente.setNuCPF(dto.getNuCPF());
        cliente.setNuTelefone(dto.getNuTelefone());
        cliente.setDsEndereco(dto.getDsEndereco());
        cliente.setEmail(dto.getEmail());

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
        return ClienteResponseDTO.builder()
                .cdCliente(cliente.getCdCliente())
                .nmCliente(cliente.getNmCliente())
                .nuCPF(cliente.getNuCPF())
                .nuTelefone(cliente.getNuTelefone())
                .dsEndereco(cliente.getDsEndereco())
                .email(cliente.getEmail())
                .ativo(cliente.getAtivo())
                .dataCadastro(cliente.getDataCadastro())
                .build();
    }
}
