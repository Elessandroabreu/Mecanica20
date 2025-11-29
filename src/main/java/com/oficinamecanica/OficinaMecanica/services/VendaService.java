package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.VendaDTO;
import com.oficinamecanica.OficinaMecanica.dto.VendaResponseDTO;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.*;
import com.oficinamecanica.OficinaMecanica.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final FaturamentoRepository faturamentoRepository;
    // ❌ NÃO PRECISA MAIS DO VendaMapper

    @Transactional
    public VendaResponseDTO criar(VendaDTO dto) {
        ClienteModel cliente = clienteRepository.findById(dto.cdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAtivo()) {
            throw new RuntimeException("Cliente inativo não pode realizar compras");
        }

        Usuario atendente = usuarioRepository.findById(dto.cdAtendente())
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        if (!atendente.getAtivo()) {
            throw new RuntimeException("Atendente inativo não pode realizar vendas");
        }

        if (!atendente.getRoles().contains(UserRole.ROLE_ATENDENTE) &&
                !atendente.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new RuntimeException("Usuário não possui perfil de atendente");
        }

        Venda venda = Venda.builder()
                .clienteModel(cliente)
                .atendente(atendente)
                .dataVenda(LocalDateTime.now())
                .vlTotal(0.0)
                .desconto(dto.desconto() != null ? dto.desconto() : 0.0)
                .formaPagamento(dto.formaPagamento())
                .build();

        Venda salva = vendaRepository.save(venda);

        if (dto.itens() != null && !dto.itens().isEmpty()) {
            adicionarItens(salva, dto.itens());
        }

        gerarFaturamento(salva);

        // ✅ Recarrega a venda com relacionamentos
        salva = vendaRepository.findById(salva.getCdVenda())
                .orElseThrow(() -> new RuntimeException("Erro ao recarregar venda"));

        return converterParaResponseDTO(salva);  // ✅ MÉTODO MANUAL
    }

    @Transactional
    public void adicionarItens(Venda venda, List<VendaDTO.ItemVendaDTO> itensDTO) {
        double total = 0.0;

        for (VendaDTO.ItemVendaDTO itemDTO : itensDTO) {
            Produto produto = produtoRepository.findById(itemDTO.cdProduto())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            if (!produto.getAtivo()) {
                throw new RuntimeException("Produto inativo: " + produto.getNmProduto());
            }

            if (produto.getQtdEstoque() < itemDTO.quantidade()) {
                throw new RuntimeException("Estoque insuficiente para: " + produto.getNmProduto());
            }

            ItemVenda item = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDTO.quantidade())
                    .vlUnitario(produto.getVlProduto())
                    .vlTotal(produto.getVlProduto() * itemDTO.quantidade())
                    .build();

            itemVendaRepository.save(item);

            produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.quantidade());
            produtoRepository.save(produto);

            total += item.getVlTotal();
        }

        venda.setVlTotal(total - venda.getDesconto());
        vendaRepository.save(venda);
    }

    @Transactional
    public void gerarFaturamento(Venda venda) {
        Faturamento faturamento = Faturamento.builder()
                .venda(venda)
                .dataVenda(venda.getDataVenda())
                .vlTotal(venda.getVlTotal())
                .formaPagamento(venda.getFormaPagamento())
                .build();

        faturamentoRepository.save(faturamento);
    }

    @Transactional(readOnly = true)
    public VendaResponseDTO buscarPorId(Integer id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        return converterParaResponseDTO(venda);  // ✅ MÉTODO MANUAL
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarTodas() {
        List<Venda> vendas = vendaRepository.findAllWithDetails();
        return vendas.stream()
                .map(this::converterParaResponseDTO)  // ✅ MÉTODO MANUAL
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarPorCliente(Integer cdCliente) {
        List<Venda> vendas = vendaRepository.findByClienteModel_CdCliente(cdCliente);
        return vendas.stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarPorAtendente(Integer cdAtendente) {
        List<Venda> vendas = vendaRepository.findByAtendente_CdUsuario(cdAtendente);
        return vendas.stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<Venda> vendas = vendaRepository.findVendasNoPeriodo(dataInicio, dataFim);
        return vendas.stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calcularTotalVendasDoDia() {
        Double total = vendaRepository.calcularTotalVendasDoDia(LocalDateTime.now());
        return total != null ? total : 0.0;
    }

    // ✅ MÉTODO PRIVADO DE CONVERSÃO (substitui o VendaMapper)
    private VendaResponseDTO converterParaResponseDTO(Venda venda) {
        if (venda == null) {
            return null;
        }

        // Mapeia cliente
        VendaResponseDTO.ClienteBasicDTO clienteDTO = null;
        if (venda.getClienteModel() != null) {
            var c = venda.getClienteModel();
            clienteDTO = new VendaResponseDTO.ClienteBasicDTO(
                    c.getCdCliente(),
                    c.getNmCliente(),
                    c.getCpf(),
                    c.getTelefone(),
                    c.getEmail()
            );
        }

        // Mapeia atendente
        VendaResponseDTO.AtendenteBasicDTO atendenteDTO = null;
        if (venda.getAtendente() != null) {
            var a = venda.getAtendente();
            atendenteDTO = new VendaResponseDTO.AtendenteBasicDTO(
                    a.getCdUsuario(),
                    a.getNmUsuario(),
                    a.getEmail()
            );
        }

        // Mapeia itens
        List<VendaResponseDTO.ItemVendaResponseDTO> itensDTO = Collections.emptyList();
        if (venda.getItens() != null && !venda.getItens().isEmpty()) {
            itensDTO = venda.getItens().stream()
                    .map(item -> new VendaResponseDTO.ItemVendaResponseDTO(
                            item.getCdItemVenda(),
                            item.getProduto().getCdProduto(),
                            item.getProduto().getNmProduto(),
                            item.getQuantidade(),
                            item.getVlUnitario(),
                            item.getVlTotal()
                    ))
                    .collect(Collectors.toList());
        }

        return new VendaResponseDTO(
                venda.getCdVenda(),
                venda.getDataVenda(),
                venda.getVlTotal(),
                venda.getDesconto(),
                venda.getFormaPagamento(),
                clienteDTO,
                atendenteDTO,
                itensDTO
        );
    }
}