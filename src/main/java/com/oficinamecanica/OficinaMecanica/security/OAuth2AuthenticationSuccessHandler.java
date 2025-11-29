package com.oficinamecanica.OficinaMecanica.security;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.UsuarioModel;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElse(null);

        if (usuario == null) {
            System.out.println("Usuário não encontrado no banco: " + email);
            String errorUrl = "http://localhost:4200?error=usuario_nao_cadastrado&email=" + email;
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        System.out.println("Usuário encontrado: " + usuario.getEmail());
        System.out.println("Roles: " + usuario.getRoles());

        if (usuario.getProvider() != AuthProvider.GOOGLE) {
            usuario.setProvider(AuthProvider.GOOGLE);
            usuario.setProviderId(providerId);
            usuarioRepository.save(usuario);
        }

        Set<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(),
                null,
                authorities
        );

        String token = tokenProvider.generateToken(newAuth);

        String redirectUrl = String.format("http://localhost:4200/auth/callback?token=%s&email=%s",
                token, email);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private UsuarioModel criarNovoUsuarioOAuth(String email, String name, String providerId) {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.ROLE_ATENDENTE);

        UsuarioModel novoUsuario = UsuarioModel.builder()
                .email(email)
                .nmUsuario(name)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .roles(roles)
                .ativo(true)
                .build();

        return usuarioRepository.save(novoUsuario);
    }
}