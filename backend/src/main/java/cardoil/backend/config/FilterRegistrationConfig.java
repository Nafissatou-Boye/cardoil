package cardoil.backend.config;

import cardoil.backend.security.ApiKeyAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterRegistrationConfig {

    // Empêche Spring Boot d'appliquer ApiKeyAuthFilter à toutes les routes automatiquement.
    // Sans ça, ce filtre bloquait aussi /api/super-admin/**, /api/admin/**, etc.
    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> desactiverEnregistrementGlobal(ApiKeyAuthFilter filtre) {
        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>(filtre);
        registration.setEnabled(false);
        return registration;
    }
}