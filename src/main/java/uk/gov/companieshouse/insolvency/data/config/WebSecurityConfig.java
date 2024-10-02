package uk.gov.companieshouse.insolvency.data.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import uk.gov.companieshouse.api.filter.CustomCorsFilter;
import uk.gov.companieshouse.insolvency.data.auth.EricTokenAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    /**
     * Configure Http Security.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.httpBasic(AbstractHttpConfigurer::disable)
                //REST APIs not enabled for cross site script headers
                .csrf(AbstractHttpConfigurer::disable) //NO SONAR
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(new EricTokenAuthenticationFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new CustomCorsFilter(List.of(HttpMethod.GET.name())), CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /**
     * Configure Web Security.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Excluding healthcheck endpoint from security filter
        return web -> web.ignoring().requestMatchers("/insolvency-data-api/healthcheck");
    }
}
