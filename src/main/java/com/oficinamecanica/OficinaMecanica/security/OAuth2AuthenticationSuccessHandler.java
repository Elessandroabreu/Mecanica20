package com.oficinamecanica.OficinaMecanica.security;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.Usuario;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

        // Buscar ou criar usuário
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> criarNovoUsuarioOAuth(email, name, providerId));

        // Gerar token JWT
        String token = tokenProvider.generateToken(authentication);

        // Redirecionar para o front-end com o token
        String redirectUrl = String.format("http://localhost:4200/auth/callback?token=%s&email=%s",
                token, email);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private Usuario criarNovoUsuarioOAuth(String email, String name, String providerId) {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.ROLE_ATENDENTE); // Novo usuário OAuth2 começa como ATENDENTE

        Usuario novoUsuario = Usuario.builder()
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