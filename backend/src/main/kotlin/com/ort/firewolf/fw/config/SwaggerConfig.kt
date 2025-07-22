package com.ort.firewolf.fw.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Firewolf API")
                    .description("人狼ゲームのバックエンドAPI")
                    .version("1.0.0")
                    .contact(
                        Contact().name("Firewolf API Support")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("/")
                        .description("Current Server")
                )
            )
    }
}