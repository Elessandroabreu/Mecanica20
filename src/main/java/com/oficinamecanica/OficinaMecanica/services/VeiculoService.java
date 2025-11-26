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
        if (veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        Cliente cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Veiculo veiculo = Veiculo.builder()
                .cliente(cliente)
                .placa(dto.placa())
                .modelo(dto.modelo())
                .marca(dto.marca())
                .ano(dto.ano())
                .cor(dto.cor())
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

        if (!veiculo.getPlaca().equals(dto.placa()) &&
                veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        veiculo.setPlaca(dto.placa());
        veiculo.setModelo(dto.modelo());
        veiculo.setMarca(dto.marca());
        veiculo.setAno(dto.ano());
        veiculo.setCor(dto.cor());

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
        return new VeiculoResponseDTO(
                veiculo.getCdVeiculo(),
                veiculo.getCliente().getCdCliente(),
                veiculo.getCliente().getNmCliente(),
                veiculo.getPlaca(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getAno(),
                veiculo.getCor()

        );
    }
}