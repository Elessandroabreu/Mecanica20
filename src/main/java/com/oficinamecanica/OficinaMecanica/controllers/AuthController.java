package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.LoginRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.UsuarioDTO;
import com.oficinamecanica.OficinaMecanica.dto.AuthResponseDTO;
import com.oficinamecanica.OficinaMecanica.dto.UsuarioResponseDTO;
import com.oficinamecanica.OficinaMecanica.security.JwtTokenProvider;
import com.oficinamecanica.OficinaMecanica.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para login, registro e autenticação OAuth2")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Login com email e senha", description = "Retorna token JWT para autenticação")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(loginRequest.email());

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                "Bearer",
                usuario
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cadastra um novo usuário LOCAL (com senha)")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody UsuarioDTO dto) {
        UsuarioResponseDTO response = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/oauth2/callback")
    public void oauth2Callback(HttpServletResponse response, Authentication authentication) throws IOException {
        String email = authentication.getName();

        String token = tokenProvider.generateToken(authentication);

        String redirectUrl = "http://localhost:4200/auth/callback?token=" + token + "&email=" + email;

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/me")
    @Operation(summary = "Obter usuário autenticado", description = "Retorna dados do usuário logado")
    public ResponseEntity<UsuarioResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "Callback de sucesso OAuth2")
    public ResponseEntity<String> oauth2Success() {
        return ResponseEntity.ok("Autenticação OAuth2 realizada com sucesso! Você pode fechar esta janela.");
    }

    @GetMapping("/oauth2/failure")
    @Operation(summary = "Callback de falha OAuth2")
    public ResponseEntity<String> oauth2Failure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Falha na autenticação OAuth2. Tente novamente.");
    }
}