package mx.lonsung.simulador.config;

import mx.lonsung.simulador.repository.UsuarioPermisoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BitacoraAuthenticationSuccessHandler bitacoraSuccessHandler;
    private final UsuarioPermisoFilter usuarioPermisoFilter;

    public SecurityConfig(BitacoraAuthenticationSuccessHandler bitacoraSuccessHandler,
                          UsuarioPermisoRepository usuarioPermisoRepository) {
        this.bitacoraSuccessHandler = bitacoraSuccessHandler;
        this.usuarioPermisoFilter = new UsuarioPermisoFilter(usuarioPermisoRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/examen", "/examen/resultados/**", "/importar-preguntas", "/agregar-examen", "/crear-examen", "/examen-en-progreso", "/examen-en-progreso/indice", "/examen-en-progreso/responder", "/examen-en-progreso/marcar-pendiente", "/examen-en-progreso/finalizar").authenticated()
                        .requestMatchers("/acceso-denegado").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")
                        .defaultSuccessUrl("/examen", true)
                        .successHandler(bitacoraSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .addFilterBefore(usuarioPermisoFilter, AuthorizationFilter.class);

        return http.build();
    }
}

