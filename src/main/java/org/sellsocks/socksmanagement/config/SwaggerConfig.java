package org.sellsocks.socksmanagement.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiDocConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title("Socks Inventory Management API")
                        .description("Socks inventory management API specification")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dmitry Bakhtin")
                                .email("dmitridorje@gmail.com"))
                        .termsOfService("Terms of service"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local Environment"));
    }
}
