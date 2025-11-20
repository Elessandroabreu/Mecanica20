package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.VeiculoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.VeiculoResponseDTO;
import com.oficinamecanica.OficinaMecanica.models.Cliente;
import com.oficinamecanica.OficinaMecanica.models.Veiculo;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import com.oficinamecanica.OficinaMecanica.repositories.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public VeiculoResponseDTO criar(VeiculoRequestDTO dto) {
        if (veiculoRepository.existsByPlaca(dto.getPlaca())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        Cliente cliente = clienteRepository.findById(dto.getCdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Veiculo veiculo = Veiculo.builder()
                .cliente(cliente)
                .placa(dto.getPlaca())
                .modelo(dto.getModelo())
                .marca(dto.getMarca())
                .ano(dto.getAno())
                .cor(dto.getCor())
                .build();

        Veiculo salvo = veiculoRepository.save(veiculo);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponseDTO buscarPorId(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        return converterParaDTO(veiculo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listarTodos() {
        return veiculoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listarPorCliente(Integer cdCliente) {
        return veiculoRepository.findByCliente_CdCliente(cdCliente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VeiculoResponseDTO atualizar(Integer id, VeiculoRequestDTO dto) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        if (!veiculo.getPlaca().equals(dto.getPlaca()) && 
            veiculoRepository.existsByPlaca(dto.getPlaca())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        veiculo.setPlaca(dto.getPlaca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setMarca(dto.getMarca());
        veiculo.setAno(dto.getAno());
        veiculo.setCor(dto.getCor());

        Veiculo atualizado = veiculoRepository.save(veiculo);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        veiculoRepository.delete(veiculo);
    }

    private VeiculoResponseDTO converterParaDTO(Veiculo veiculo) {
        return VeiculoResponseDTO.builder()
                .cdVeiculo(veiculo.getCdVeiculo())
                .cdCliente(veiculo.getCliente().getCdCliente())
                .nmCliente(veiculo.getCliente().getNmCliente())
                .placa(veiculo.getPlaca())
                .modelo(veiculo.getModelo())
                .marca(veiculo.getMarca())
                .ano(veiculo.getAno())
                .cor(veiculo.getCor())
                .dataCadastro(veiculo.getDataCadastro())
                .build();
    }
}
