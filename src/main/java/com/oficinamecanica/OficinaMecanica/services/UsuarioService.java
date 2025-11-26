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

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (dto.nuCPF() != null && usuarioRepository.existsByNuCPF(dto.nuCPF())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nmUsuario(dto.nmUsuario())
                .email(dto.email())
                .password(dto.password() != null ? passwordEncoder.encode(dto.password()) : null)
                .provider(dto.provider())
                .roles(dto.roles())
                .nuTelefone(dto.nuTelefone())
                .nuCPF(dto.nuCPF())
                .providerId(dto.providerId())
                .ativo(dto.ativo() != null ? dto.ativo() : true)
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

        if (!usuario.getEmail().equals(dto.email()) &&
                usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        usuario.setNmUsuario(dto.nmUsuario());
        usuario.setEmail(dto.email());
        if (dto.password() != null && !dto.password().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.password()));
        }
        usuario.setNuTelefone(dto.nuTelefone());
        usuario.setNuCPF(dto.nuCPF());
        usuario.setRoles(dto.roles());
        usuario.setAtivo(dto.ativo() != null ? dto.ativo() : true);

        Usuario atualizado = usuarioRepository.save(usuario);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    private UsuarioResponseDTO converterParaDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getCdUsuario(),
                usuario.getNmUsuario(),
                usuario.getEmail(),
                usuario.getProvider(),
                usuario.getRoles(),
                usuario.getNuTelefone(),
                usuario.getNuCPF(),
                usuario.getAtivo()

        );
    }
}