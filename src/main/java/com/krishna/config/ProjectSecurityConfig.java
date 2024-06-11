package com.krishna.config;

import com.krishna.filter.CsrfCookieFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.sql.DataSource;
import java.util.Collections;

@Configuration
public class ProjectSecurityConfig {
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(requests -> requests
                .requestMatchers("/myCards", "/myAccount", "/myBalance", "/myLoans", "/user").authenticated()
                .requestMatchers("/notices", "/register", "/contact").permitAll()
        );

        httpSecurity.formLogin(Customizer.withDefaults());
        httpSecurity.httpBasic(Customizer.withDefaults());

        // Below is the configuration for creating JSessionId for UI app
        // Without this we have to share credentials with each request from FE
        httpSecurity
                .securityContext(securityCont -> securityCont.requireExplicitSave(false))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        // CSRF configuration

        /*
        * Below configuration will set XSRF-TOKEN in cookie and while reading it will check X-XSRF-TOKEN from headers by default
        * More details could be found in CookieCsrfTokenRepository class
        *
        * While sending response from BE we are using filter/CsrfCookieFilter to send csrf token in cookie
        * */

        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        csrfTokenRequestAttributeHandler.setCsrfRequestAttributeName("_csrf"); // -> this is also the default name, adding here just for readability

//        httpSecurity.csrf(csrfConfig -> csrfConfig.disable());
        httpSecurity
                .csrf(csrfConfig -> csrfConfig
                        .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/register", "/contact") // ignoring csrf check for these paths
                        .csrfTokenRepository(/*new CookieCsrfTokenRepository()*/CookieCsrfTokenRepository.withHttpOnlyFalse()) // with http only as false, the FE is allowed to read
                                                                                            // cookie value set for csrf token
                                                                                            // this value can be used by FE to add in header for each request
                )
                .addFilterAfter(
                        new CsrfCookieFilter(),
                        BasicAuthenticationFilter.class
                );




        // CORS configuration
        httpSecurity.cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                corsConfiguration.setAllowCredentials(true);
                corsConfiguration.setMaxAge(3600L); // 3600 seconds -> 1 hr -> it means browser can remember the configuration upto 1 hr
                return corsConfiguration;
            }
        }));

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
