package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.VendaRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.VendaResponseDTO;
import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
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
    public VendaResponseDTO criar(VendaRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getCdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Usuario atendente = usuarioRepository.findById(dto.getCdAtendente())
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        Venda venda = Venda.builder()
                .cliente(cliente)
                .atendente(atendente)
                .dataVenda(LocalDateTime.now())
                .vlTotal(0.0)
                .desconto(dto.getDesconto() != null ? dto.getDesconto() : 0.0)
                .formaPagamento(dto.getFormaPagamento())
                .build();

        Venda salva = vendaRepository.save(venda);

        if (dto.getItens() != null && !dto.getItens().isEmpty()) {
            adicionarItens(salva, dto.getItens());
        }

        gerarFaturamento(salva);

        return converterParaDTO(salva);
    }

    @Transactional
    public void adicionarItens(Venda venda, List<VendaRequestDTO.ItemVendaDTO> itensDTO) {
        double total = 0.0;

        for (VendaRequestDTO.ItemVendaDTO itemDTO : itensDTO) {
            Produto produto = produtoRepository.findById(itemDTO.getCdProduto())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            if (produto.getQtdEstoque() < itemDTO.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNmProduto());
            }

            ItemVenda item = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDTO.getQuantidade())
                    .vlUnitario(produto.getVlVenda())
                    .vlTotal(produto.getVlVenda() * itemDTO.getQuantidade())
                    .build();

            itemVendaRepository.save(item);

            produto.setQtdEstoque(produto.getQtdEstoque() - itemDTO.getQuantidade());
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
                .formaPagamento(FormaPagamento.valueOf(venda.getFormaPagamento().name()))
                .build();

        faturamentoRepository.save(faturamento);
    }

    @Transactional(readOnly = true)
    public VendaResponseDTO buscarPorId(Integer id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        return converterParaDTO(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarTodas() {
        return vendaRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarPorCliente(Integer cdCliente) {
        return vendaRepository.findByCliente_CdCliente(cdCliente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarPorAtendente(Integer cdAtendente) {
        return vendaRepository.findByAtendente_CdUsuario(cdAtendente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return vendaRepository.findVendasNoPeriodo(dataInicio, dataFim).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double calcularTotalVendasDoDia() {
        Double total = vendaRepository.calcularTotalVendasDoDia(LocalDateTime.now());
        return total != null ? total : 0.0;
    }

    private VendaResponseDTO converterParaDTO(Venda venda) {
        return VendaResponseDTO.builder()
                .cdVenda(venda.getCdVenda())
                .cdCliente(venda.getCliente().getCdCliente())
                .nmCliente(venda.getCliente().getNmCliente())
                .cdAtendente(venda.getAtendente().getCdUsuario())
                .nmAtendente(venda.getAtendente().getNmUsuario())
                .dataVenda(venda.getDataVenda())
                .vlTotal(venda.getVlTotal())
                .desconto(venda.getDesconto())
                .formaPagamento(venda.getFormaPagamento())
                .dataCadastro(venda.getDataCadastro())
                .build();
    }
}
