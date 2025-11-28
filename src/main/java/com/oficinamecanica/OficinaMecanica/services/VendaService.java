package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.VendaDTO;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.*;
import com.oficinamecanica.OficinaMecanica.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
    public VendaDTO criar(VendaDTO dto) {
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

        return converterParaDTO(salva);
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
    public VendaDTO buscarPorId(Integer id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        return converterParaDTO(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaDTO> listarTodas() {
        return vendaRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaDTO> listarPorCliente(Integer cdCliente) {
        return vendaRepository.findByClienteModel_CdCliente(cdCliente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaDTO> listarPorAtendente(Integer cdAtendente) {
        return vendaRepository.findByAtendente_CdUsuario(cdAtendente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaDTO> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return vendaRepository.findVendasNoPeriodo(dataInicio, dataFim).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calcularTotalVendasDoDia() {
        Double total = vendaRepository.calcularTotalVendasDoDia(LocalDateTime.now());
        return total != null ? total : 0.0;
    }

    private VendaDTO converterParaDTO(Venda venda) {
        List<VendaDTO.ItemVendaDTO> itensDTO = venda.getItens() != null
                ? venda.getItens().stream()
                .map(item -> new VendaDTO.ItemVendaDTO(
                        item.getProduto().getCdProduto(),
                        item.getQuantidade()
                ))
                .collect(Collectors.toList())
                : List.of();

        return new VendaDTO(
                venda.getClienteModel().getCdCliente(),
                venda.getAtendente().getCdUsuario(),
                venda.getDesconto(),
                venda.getFormaPagamento(),
                itensDTO
        );
    }
}