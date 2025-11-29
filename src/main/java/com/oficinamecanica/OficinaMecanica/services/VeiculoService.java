package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.VeiculoDTO;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import com.oficinamecanica.OficinaMecanica.models.VeiculoModel;
import com.oficinamecanica.OficinaMecanica.repositories.ClienteRepository;
import com.oficinamecanica.OficinaMecanica.repositories.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de negócio de Veículos
 * Gerencia: carros, motos e outros veículos dos clientes
 *
 * IMPORTANTE: Veículo usa HARD DELETE (remove do banco)
 * diferente de Cliente/Produto que usam Soft Delete
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VeiculoService {


    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public VeiculoDTO criar(VeiculoDTO dto) {
        log.info("Criando veículo: Placa {}", dto.placa());

        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        VeiculoModel veiculo = VeiculoModel.builder()
                .clienteModel(cliente)
                .placa(dto.placa().toUpperCase())
                .modelo(dto.modelo())
                .marca(dto.marca())
                .ano(dto.ano())
                .cor(dto.cor())
                .build();

        VeiculoModel salvo = veiculoRepository.save(veiculo);

        log.info("Veículo criado: ID {} - Placa {}",
                salvo.getCdVeiculo(), salvo.getPlaca());

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public VeiculoDTO buscarPorId(Integer id) {
        VeiculoModel veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        return converterParaDTO(veiculo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarTodos() {
        log.info("📋 Listando todos os veículos");

        return veiculoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarPorCliente(Integer cdCliente) {
        log.info("Listando veículos do cliente: {}", cdCliente);

        return veiculoRepository.findByClienteModel_CdCliente(cdCliente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VeiculoDTO atualizar(Integer id, VeiculoDTO dto) {
        log.info("🔄 Atualizando veículo ID: {}", id);

        VeiculoModel veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

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


        VeiculoModel atualizado = veiculoRepository.save(veiculo);

        log.info("Veículo atualizado: Placa {}", atualizado.getPlaca());

        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        log.info("Deletando veículo ID: {}", id);

        VeiculoModel veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        veiculoRepository.delete(veiculo);

        log.info("Veículo removido permanentemente");
    }

    private VeiculoDTO converterParaDTO(VeiculoModel veiculo) {
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