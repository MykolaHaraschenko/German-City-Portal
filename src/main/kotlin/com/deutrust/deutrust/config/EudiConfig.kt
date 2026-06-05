package com.deutrust.deutrust.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class EudiConfig {

    /**
     * Declares the ObjectMapper bean with Kotlin Module pre-registered.
     * This guarantees that Jackson is always available in the ApplicationContext.
     */
    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }

    /**
     * Customizes the OpenAPI documentation metadata presented in Swagger UI.
     */
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("DeuTrust EUDI Wallet API")
                    .version("1.0.0")
                    .description("Backend service MVP for Small & Medium Businesses integrating European Union Digital Identity (EUDI) Wallet OID4VP and OID4VCI standards.")
            )
    }

    /**
     * Configures a global CORS mapping allowing all cross-origin requests
     * from development frontends (e.g., Angular on localhost:4200 or custom tunneling proxies).
     */
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                    .allowedHeaders("*")
                    .exposedHeaders("Authorization", "Content-Type", "Accept")
                    .maxAge(3600)
            }
        }
    }
}
