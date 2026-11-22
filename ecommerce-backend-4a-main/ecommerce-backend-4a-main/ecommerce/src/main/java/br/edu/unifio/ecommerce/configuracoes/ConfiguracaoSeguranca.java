package br.edu.unifio.ecommerce.configuracoes;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class ConfiguracaoSeguranca {

    @Bean
    public CorsConfigurationSource configuracaoCors() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(
    "http://localhost:4200",
    "http://127.0.0.1:4200",
    "https://e-sports-lyart.vercel.app"
));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", cors);
        return origem;
    }

    @Bean
    public PasswordEncoder codificadorSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService usuarios(
            PasswordEncoder codificadorSenha,
            @Value("${ESPORTS_ORGANIZADOR_SENHA}") String senhaOrganizador,
            @Value("${ESPORTS_CONSULTA_SENHA}") String senhaConsulta) {
        if (senhaOrganizador.isBlank() || senhaConsulta.isBlank()) {
            throw new IllegalArgumentException("Configure senhas de teste não vazias nas variáveis de ambiente.");
        }

        var organizador = User.withUsername("ORGANIZADOR")
                .password(codificadorSenha.encode(senhaOrganizador))
                .roles("ORGANIZADOR")
                .build();
        var consulta = User.withUsername("CONSULTA")
                .password(codificadorSenha.encode(senhaConsulta))
                .roles("CONSULTA")
                .build();

        return new InMemoryUserDetailsManager(organizador, consulta);
    }

    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        var entradaBasic = new BasicAuthenticationEntryPoint();
        entradaBasic.setRealmName("E-sports");
        entradaBasic.afterPropertiesSet();

        http
                .cors(cors -> cors.configurationSource(configuracaoCors()))
                // API para clientes REST com credenciais em cada requisição, sem sessão.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(autorizacao -> autorizacao
                        // Preserva o status original em encaminhamentos internos de erro.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/torneios", "/torneios/**",
                                "/modalidades", "/modalidades/**",
                                "/partidas", "/partidas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/atletas", "/atletas/**",
                                "/participacoes-atletas", "/participacoes-atletas/**",
                                "/inscricoes", "/inscricoes/**", "/estatisticas", "/estatisticas/**")
                            .hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.GET, "/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/**").hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.PUT, "/**").hasRole("ORGANIZADOR")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ORGANIZADOR")
                        .anyRequest().denyAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(entradaBasic))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(entradaBasic)
                        .accessDeniedHandler((requisicao, resposta, erro) ->
                                resposta.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.")));

        return http.build();
    }
}
