package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.VeiculoDTO;
import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import com.oficinamecanica.OficinaMecanica.models.Veiculo;
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

    // Injeções
    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    /**
     * CRIAR NOVO VEÍCULO
     */
    @Transactional
    public VeiculoDTO criar(VeiculoDTO dto) {
        log.info("🚗 Criando veículo: Placa {}", dto.placa());

        // 1. VALIDAR SE CLIENTE EXISTE
        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2. VALIDAR PLACA ÚNICA
        if (veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        // 3. CRIAR VEÍCULO
        Veiculo veiculo = Veiculo.builder()
                .clienteModel(cliente)
                .placa(dto.placa().toUpperCase()) // ⚠️ Placa sempre MAIÚSCULA
                .modelo(dto.modelo())
                .marca(dto.marca())
                .ano(dto.ano())
                .cor(dto.cor())
                .build();

        // 4. SALVAR
        Veiculo salvo = veiculoRepository.save(veiculo);

        log.info("✅ Veículo criado: ID {} - Placa {}",
                salvo.getCdVeiculo(), salvo.getPlaca());

        return converterParaDTO(salvo);
    }

    /**
     * BUSCAR VEÍCULO POR ID
     */
    @Transactional(readOnly = true)
    public VeiculoDTO buscarPorId(Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        return converterParaDTO(veiculo);
    }

    /**
     * LISTAR TODOS OS VEÍCULOS
     */
    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarTodos() {
        log.info("📋 Listando todos os veículos");

        return veiculoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR VEÍCULOS DE UM CLIENTE
     * Útil para mostrar todos os carros de um cliente específico
     */
    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarPorCliente(Integer cdCliente) {
        log.info("🚗 Listando veículos do cliente: {}", cdCliente);

        return veiculoRepository.findByClienteModel_CdCliente(cdCliente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * ATUALIZAR VEÍCULO
     */
    @Transactional
    public VeiculoDTO atualizar(Integer id, VeiculoDTO dto) {
        log.info("🔄 Atualizando veículo ID: {}", id);

        // 1. BUSCAR VEÍCULO EXISTENTE
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        // 2. VALIDAR SE CLIENTE EXISTE
        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 3. VALIDAR PLACA ÚNICA (exceto para o próprio veículo)
        if (!veiculo.getPlaca().equalsIgnoreCase(dto.placa()) &&
                veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("Placa já cadastrada");
        }

        // 4. ATUALIZAR CAMPOS
        veiculo.setClienteModel(cliente);
        veiculo.setPlaca(dto.placa().toUpperCase());
        veiculo.setModelo(dto.modelo());
        veiculo.setMarca(dto.marca());
        veiculo.setAno(dto.ano());
        veiculo.setCor(dto.cor());

        // 5. SALVAR E RETORNAR
        Veiculo atualizado = veiculoRepository.save(veiculo);

        log.info("✅ Veículo atualizado: Placa {}", atualizado.getPlaca());

        return converterParaDTO(atualizado);
    }

    /**
     * DELETAR VEÍCULO (HARD DELETE)
     *
     * ⚠️ ATENÇÃO: Este é um HARD DELETE!
     * O veículo é REMOVIDO DO BANCO permanentemente
     *
     * Por quê hard delete?
     * - Veículos não têm impacto em históricos críticos
     * - Se o cliente vendeu o carro, não faz sentido mantê-lo
     */
    @Transactional
    public void deletar(Integer id) {
        log.info("🗑️ Deletando veículo ID: {}", id);

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        // ⚠️ REMOVE DO BANCO (hard delete)
        veiculoRepository.delete(veiculo);

        log.info("✅ Veículo removido permanentemente");
    }

    // ========== MÉTODO AUXILIAR ==========

    /**
     * CONVERTER MODEL PARA DTO
     */
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