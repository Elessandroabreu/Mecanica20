package com.oficinamecanica.OficinaMecanica.security;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.models.UsuarioModel;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (response.isCommitted()) {
            log.warn("Response já foi committed. Não é possível redirecionar.");
            return;
        }

        try {

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String providerId = oAuth2User.getAttribute("sub");

            log.info("Autenticação Google bem-sucedida para: {}", email);


            UsuarioModel usuario = usuarioRepository.findByEmail(email).orElse(null);


            if (usuario == null) {
                log.error("Usuário não encontrado no banco: {}", email);
                String errorUrl = UriComponentsBuilder
                        .fromUriString(frontendUrl + "/auth/login")
                        .queryParam("error", "user_not_registered")
                        .queryParam("message", "Usuário não cadastrado. Contate o administrador.")
                        .build()
                        .toUriString();

                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            log.info("Usuário encontrado: {} | Roles: {}", usuario.getEmail(), usuario.getRoles());


            if (usuario.getProvider() != AuthProvider.GOOGLE) {
                log.info("Atualizando provider do usuário para GOOGLE");
                usuario.setProvider(AuthProvider.GOOGLE);
                usuario.setProviderId(providerId);
                usuarioRepository.save(usuario);
            }


            Set<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(Collectors.toSet());

            String token = tokenProvider.generateTokenFromEmailAndRoles(usuario.getEmail(), authorities);

            log.info("Token JWT gerado: {}...", token.substring(0, 20));


            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("token", token)
                    .build()
                    .toUriString();

            log.info("🔗 Redirecionando para: {}", redirectUrl);

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("Erro no OAuth2 Success Handler: {}", e.getMessage(), e);


            String errorUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/auth/login")
                    .queryParam("error", "google_auth_failed")
                    .queryParam("message", "Erro na autenticação. Tente novamente.")
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}