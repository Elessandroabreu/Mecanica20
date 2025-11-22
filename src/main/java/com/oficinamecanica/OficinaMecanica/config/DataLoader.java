package com.oficinamecanica.OficinaMecanica.config;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.Usuario;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * DataLoader para criar usuários padrão na inicialização da aplicação
 *
 * Usuários criados:
 * - Admin: admin@oficina.com / senha123
 * - Atendente: atendente@oficina.com / senha123
 * - Mecânico: mecanico@oficina.com / senha123
 */
@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {


            if (usuarioRepository.count() > 0) {
                System.out.println("✅ Usuários já existem no banco de dados.");
                System.out.println("📊 Total de usuários: " + usuarioRepository.count());
                System.out.println();
                return;
            }

            System.out.println("🔨 Criando usuários padrão...\n");

            // Senha padrão para todos
            String senhaTexto = "senha123";
            String senhaCriptografada = passwordEncoder.encode(senhaTexto);

            // 1. ADMIN
            Usuario admin = Usuario.builder()
                    .nmUsuario("João Admin Silva")
                    .email("admin@oficina.com")
                    .password(senhaCriptografada)
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(UserRole.ROLE_ADMIN))
                    .nuTelefone("(48) 99999-0001")
                    .nuCPF("123.456.789-01")
                    .ativo(true)
                    .build();

            usuarioRepository.save(admin);
            System.out.println("✅ Admin criado: " + admin.getEmail());

            // 2. ATENDENTE
            Usuario atendente = Usuario.builder()
                    .nmUsuario("Maria Atendente Santos")
                    .email("atendente@oficina.com")
                    .password(senhaCriptografada)
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(UserRole.ROLE_ATENDENTE))
                    .nuTelefone("(48) 99999-0002")
                    .nuCPF("234.567.890-12")
                    .ativo(true)
                    .build();

            usuarioRepository.save(atendente);
            System.out.println("✅ Atendente criado: " + atendente.getEmail());

            // 3. MECÂNICO
            Usuario mecanico = Usuario.builder()
                    .nmUsuario("Carlos Mecânico Souza")
                    .email("mecanico@oficina.com")
                    .password(senhaCriptografada)
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(UserRole.ROLE_MECANICO))
                    .nuTelefone("(48) 99999-0003")
                    .nuCPF("345.678.901-23")
                    .ativo(true)
                    .build();

            usuarioRepository.save(mecanico);
            System.out.println("✅ Mecânico criado: " + mecanico.getEmail());

            System.out.println("👤 ADMIN");
            System.out.println("   Email: admin@oficina.com");
            System.out.println("   Senha: senha123");
            System.out.println();
            System.out.println("👤 ATENDENTE");
            System.out.println("   Email: atendente@oficina.com");
            System.out.println("   Senha: senha123");
            System.out.println();
            System.out.println("👤 MECÂNICO");
            System.out.println("   Email: mecanico@oficina.com");
            System.out.println("   Senha: senha123");



            System.out.println("Verificando criação...");
            usuarioRepository.findAll().forEach(u -> {
                System.out.println("   ✓ " + u.getEmail() + " | Roles: " + u.getRoles());
            });
            System.out.println();
        };
    }
}