package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.VeiculoDTO;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
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

    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarTodos() {
        return veiculoRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VeiculoDTO buscarPorId(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        return converterParaDTO(veiculo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarPorCliente(Integer cdCliente) {
        return veiculoRepository.findByClienteModel_CdCliente(cdCliente)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VeiculoDTO criar(VeiculoDTO dto) {
        // Validar se cliente existe
        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Validar se placa já existe
        if (veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        Veiculo veiculo = new Veiculo();
        veiculo.setClienteModel(cliente);
        veiculo.setPlaca(dto.placa().toUpperCase());
        veiculo.setModelo(dto.modelo());
        veiculo.setMarca(dto.marca());
        veiculo.setAno(dto.ano());
        veiculo.setCor(dto.cor());

        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);
        return converterParaDTO(veiculoSalvo);
    }

    @Transactional
    public VeiculoDTO atualizar(Integer id, VeiculoDTO dto) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        // Validar se cliente existe
        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Validar se placa já existe (exceto para o próprio veículo)
        if (!veiculo.getPlaca().equalsIgnoreCase(dto.placa()) &&
                veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        veiculo.setClienteModel(cliente);
        veiculo.setPlaca(dto.placa().toUpperCase());
        veiculo.setModelo(dto.modelo());
        veiculo.setMarca(dto.marca());
        veiculo.setAno(dto.ano());
        veiculo.setCor(dto.cor());

        Veiculo veiculoAtualizado = veiculoRepository.save(veiculo);
        return converterParaDTO(veiculoAtualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        // Hard delete - remove do banco de dados
        veiculoRepository.delete(veiculo);
    }

    private VeiculoDTO converterParaDTO(Veiculo veiculo) {
        return new VeiculoDTO(
                veiculo.getCdVeiculo(),
                veiculo.getClienteModel().getCdCliente(),
                veiculo.getPlaca(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getAno(),
                veiculo.getCor()
        );
    }
}