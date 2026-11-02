package com.electronicstore.inventory.config;

import com.electronicstore.inventory.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight: the browser sends an OPTIONS request (with no
                        // Authorization header) before every cross-origin request that carries
                        // a custom header - which is every authenticated call this frontend
                        // makes. Without this rule, the AuthorizationFilter treats that OPTIONS
                        // request as anonymous and denies it, which makes the browser block the
                        // real request as a CORS failure (surfaces as a bodyless network error,
                        // not a normal 401/403 with a JSON body). Must stay first in the chain.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/promotions/**").permitAll()

                        // Inventory - manager+ only for writes
                        .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")

                        // Product/category writes - inventory manager and above
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")

                        // Sales/POS - cashier and above can process transactions
                        .requestMatchers(HttpMethod.POST, "/api/transactions/**").hasAnyRole(
                                "CASHIER", "SALES_ASSOCIATE", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/transactions/*/void").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")

                        // Supplier management - inventory manager and above
                        .requestMatchers(HttpMethod.POST, "/api/suppliers/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")

                        // Purchase orders - inventory manager and above create/manage; approval reserved for manager+
                        .requestMatchers(HttpMethod.POST, "/api/purchase-orders", "/api/purchase-orders/auto-generate").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/approve").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/*/receive",
                                "/api/purchase-orders/*/mark-ordered", "/api/purchase-orders/*/cancel").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")

                        // Reports - sales figures visible to sales staff and up; inventory
                        // valuation/margins restricted to inventory manager and up (cost data)
                        .requestMatchers(HttpMethod.GET, "/api/reports/sales/**").hasAnyRole(
                                "SALES_ASSOCIATE", "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/sales/**").hasAnyRole(
                                "SALES_ASSOCIATE", "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reports/inventory/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")

                        // Promotions - inventory manager and above can manage
                        .requestMatchers(HttpMethod.POST, "/api/promotions/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/promotions/**").hasAnyRole(
                                "INVENTORY_MANAGER", "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/promotions/**").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")

                        // Audit logs - store manager (limited) and admin, per Appendix A
                        .requestMatchers(HttpMethod.GET, "/api/admin/audit-logs/**").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")

                        // Customer loyalty adjustments - staff only, not the customer's own account
                        .requestMatchers(HttpMethod.POST, "/api/customers/*/loyalty").hasAnyRole(
                                "CASHIER", "SALES_ASSOCIATE", "STORE_MANAGER", "ADMIN")

                        // Notifications - broadcast/send reserved for store manager and admin
                        .requestMatchers(HttpMethod.POST, "/api/admin/notifications").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")

                        // User management - store manager gets read-only (limited), admin gets full
                        // control including deactivation and role changes, per Appendix A
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/**").hasAnyRole(
                                "STORE_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/**").hasRole("ADMIN")

                        // Admin-only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}