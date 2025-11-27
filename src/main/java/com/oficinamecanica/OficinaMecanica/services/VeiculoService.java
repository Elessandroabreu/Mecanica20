package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.VeiculoRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.VeiculoResponseDTO;
import com.oficinamecanica.OficinaMecanica.models.Cliente;
import com.oficinamecanica.OficinaMecanica.models.Veiculo;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import com.oficinamecanica.OficinaMecanica.repositories.VeiculoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listarTodos() {
        return veiculoRepository.findAll() // ✅ Simples findAll
                .stream()
                .map(VeiculoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VeiculoResponseDTO buscarPorId(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
        return new VeiculoResponseDTO(veiculo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listarPorCliente(Integer cdCliente) {
        return veiculoRepository.findByClienteCdCliente(cdCliente) // ✅ Sem "AndAtivoTrue"
                .stream()
                .map(VeiculoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public VeiculoResponseDTO criar(VeiculoRequestDTO dto) {
        // Valida se cliente existe
        Cliente cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        // Valida se placa já existe
        if (veiculoRepository.existsByPlaca(dto.placa())) {
            throw new IllegalArgumentException("Placa já cadastrada");
        }

        Veiculo veiculo = new Veiculo();
        veiculo.setCliente(cliente);
        veiculo.setPlaca(dto.placa().toUpperCase());
        veiculo.setModelo(dto.modelo());
        veiculo.setMarca(dto.marca());
        veiculo.setAno(dto.ano());
        veiculo.setCor(dto.cor());
        // ✅ REMOVIDO: veiculo.setAtivo(true);

        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);
        return new VeiculoResponseDTO(veiculoSalvo);
    }

    @Transactional
    public VeiculoResponseDTO atualizar(Integer id, VeiculoRequestDTO dto) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));

        // Valida se cliente existe
        Cliente cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        // Valida se placa já existe (exceto para o próprio veículo)
        if (!veiculo.getPlaca().equalsIgnoreCase(dto.placa()) &&
                veiculoRepository.existsByPlaca(dto.placa())) {
            throw new IllegalArgumentException("Placa já cadastrada");
        }

        veiculo.setCliente(cliente);
        veiculo.setPlaca(dto.placa().toUpperCase());
        veiculo.setModelo(dto.modelo());
        veiculo.setMarca(dto.marca());
        veiculo.setAno(dto.ano());
        veiculo.setCor(dto.cor());

        Veiculo veiculoAtualizado = veiculoRepository.save(veiculo);
        return new VeiculoResponseDTO(veiculoAtualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));

        // ✅ HARD DELETE - Remove do banco de dados
        veiculoRepository.delete(veiculo);
    }
}