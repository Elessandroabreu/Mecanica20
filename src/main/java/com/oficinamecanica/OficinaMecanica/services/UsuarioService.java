package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.request.UsuarioRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.response.UsuarioResponseDTO;
import com.oficinamecanica.OficinaMecanica.models.Usuario;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        // Validar se email já existe
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Validar se CPF já existe (se informado)
        if (dto.getNuCPF() != null && usuarioRepository.existsByNuCPF(dto.getNuCPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nmUsuario(dto.getNmUsuario())
                .email(dto.getEmail())
                .password(dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null)
                .provider(dto.getProvider())
                .roles(dto.getRoles())
                .nuTelefone(dto.getNuTelefone())
                .nuCPF(dto.getNuCPF())
                .providerId(dto.getProviderId())
                .ativo(dto.getAtivo())
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return converterParaDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return converterParaDTO(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarAtivos() {
        return usuarioRepository.findByAtivoTrue().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarMecanicosAtivos() {
        return usuarioRepository.findMecanicosAtivos().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarAtendentesAtivos() {
        return usuarioRepository.findAtendentesAtivos().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Integer id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Validar email se foi alterado
        if (!usuario.getEmail().equals(dto.getEmail()) && 
            usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        usuario.setNmUsuario(dto.getNmUsuario());
        usuario.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        usuario.setNuTelefone(dto.getNuTelefone());
        usuario.setNuCPF(dto.getNuCPF());
        usuario.setRoles(dto.getRoles());
        usuario.setAtivo(dto.getAtivo());

        Usuario atualizado = usuarioRepository.save(usuario);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Soft delete
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    private UsuarioResponseDTO converterParaDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .cdUsuario(usuario.getCdUsuario())
                .nmUsuario(usuario.getNmUsuario())
                .email(usuario.getEmail())
                .provider(usuario.getProvider())
                .roles(usuario.getRoles())
                .nuTelefone(usuario.getNuTelefone())
                .nuCPF(usuario.getNuCPF())
                .ativo(usuario.getAtivo())
                .dataCadastro(usuario.getDataCadastro())
                .dataAtualizacao(usuario.getDataAtualizacao())
                .build();
    }
}
