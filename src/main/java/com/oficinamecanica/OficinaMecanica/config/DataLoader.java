package com.oficinamecanica.OficinaMecanica.config;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.enums.UserRole;
import com.oficinamecanica.OficinaMecanica.models.UsuarioModel;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {


            if (usuarioRepository.count() > 0) {
                System.out.println("Usuários já existem no banco de dados.");
                System.out.println("Total de usuários: " + usuarioRepository.count());
                System.out.println();
                return;
            }

            System.out.println("Criando usuários padrão\n");
            String senhaTexto = "senha123";
            String senhaCriptografada = passwordEncoder.encode(senhaTexto);

            UsuarioModel admin = UsuarioModel.builder()
                    .nmUsuario("João Admin Silva")
                    .email("admin@oficina.com")
                    .senha(senhaCriptografada)
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(UserRole.ROLE_ADMIN))
                    .telefone("(48) 99999-0001")
                    .cpf("123.456.789-01")
                    .ativo(true)
                    .build();

            usuarioRepository.save(admin);
            System.out.println("Admin criado: " + admin.getEmail());

            UsuarioModel atendente = UsuarioModel.builder()
                    .nmUsuario("Maria Atendente Santos")
                    .email("atendente@oficina.com")
                    .senha(senhaCriptografada)
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(UserRole.ROLE_ATENDENTE))
                    .telefone("(48) 99999-0002")
                    .cpf("234.567.890-12")
                    .ativo(true)
                    .build();

            usuarioRepository.save(atendente);
            System.out.println("Atendente criado: " + atendente.getEmail());

            UsuarioModel mecanico = UsuarioModel.builder()
                    .nmUsuario("Carlos Mecânico Souza")
                    .email("mecanico@oficina.com")
                    .senha(senhaCriptografada)
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(UserRole.ROLE_MECANICO))
                    .telefone("(48) 99999-0003")
                    .cpf("345.678.901-23")
                    .ativo(true)
                    .build();

            usuarioRepository.save(mecanico);
            System.out.println("Mecânico criado: " + mecanico.getEmail());

            System.out.println("ADMIN");
            System.out.println("   Email: admin@oficina.com");
            System.out.println("   Senha: senha123");
            System.out.println();
            System.out.println("ATENDENTE");
            System.out.println("   Email: atendente@oficina.com");
            System.out.println("   Senha: senha123");
            System.out.println();
            System.out.println("MECÂNICO");
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