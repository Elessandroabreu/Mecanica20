package com.oficinamecanica.OficinaMecanica.services;

import com.oficinamecanica.OficinaMecanica.dto.UsuarioDTO;
import com.oficinamecanica.OficinaMecanica.dto.UsuarioResponseDTO;
import com.oficinamecanica.OficinaMecanica.models.UsuarioModel;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioDTO dto) {
        log.info("👤 Criando usuário: {}", dto.email());

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (dto.cpf() != null && usuarioRepository.existsByCpf(dto.cpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        UsuarioModel usuario = UsuarioModel.builder()
                .nmUsuario(dto.nmUsuario())
                .email(dto.email())
                .senha(dto.senha() != null ? passwordEncoder.encode(dto.senha()) : null)
                .provider(dto.provider())
                .roles(dto.roles())
                .telefone(dto.telefone())
                .cpf(dto.cpf())
                .providerId(dto.providerId())
                .ativo(dto.ativo() != null ? dto.ativo() : true)
                .build();

        UsuarioModel salvo = usuarioRepository.save(usuario);

        log.info("Usuário criado: ID {} - {}", salvo.getCdUsuario(), salvo.getEmail());

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Integer id) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return converterParaDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        UsuarioModel usuario = usuarioRepository.findByEmail(email)
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
    public UsuarioResponseDTO atualizar(Integer id, UsuarioDTO dto) {
        log.info("Atualizando usuário ID: {}", id);

        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.getEmail().equals(dto.email()) &&
                usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (dto.cpf() != null &&
                !dto.cpf().equals(usuario.getCpf()) &&
                usuarioRepository.existsByCpf(dto.cpf())) {
            throw new RuntimeException("CPF já cadastrado");
        }

        usuario.setNmUsuario(dto.nmUsuario());
        usuario.setEmail(dto.email());

        if (dto.senha() != null && !dto.senha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        usuario.setTelefone(dto.telefone());
        usuario.setCpf(dto.cpf());
        usuario.setRoles(dto.roles());
        usuario.setAtivo(dto.ativo() != null ? dto.ativo() : true);

        UsuarioModel atualizado = usuarioRepository.save(usuario);

        log.info("Usuário atualizado: {}", atualizado.getEmail());

        return converterParaDTO(atualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        log.info("Deletando usuário ID: {}", id);

        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        log.info("Usuário marcado como inativo");
    }

    private UsuarioResponseDTO converterParaDTO(UsuarioModel usuario) {
        return new UsuarioResponseDTO(
                usuario.getCdUsuario(),
                usuario.getNmUsuario(),
                usuario.getEmail(),
                usuario.getProvider(),
                usuario.getRoles(),
                usuario.getTelefone(),
                usuario.getCpf(),
                usuario.getAtivo()
        );
    }
}