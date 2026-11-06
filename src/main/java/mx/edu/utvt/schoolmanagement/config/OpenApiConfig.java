package mx.edu.utvt.schoolmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI schoolManagementOpenApi() {
        return new OpenAPI()
                .info(new Info().title("UTVT School Management API").version("v1")
                        .description("Layered REST API for student management at UTVT.")
                        .contact(new Contact().name("UTVT")))
                .servers(List.of(new Server().url("/")));
    }
}
