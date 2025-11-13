package crudjava.crudjava.config;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(
        @org.springframework.lang.NonNull CorsRegistry registry
    ) {
        registry
            .addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }

    @Override
    public void configureContentNegotiation(
        @org.springframework.lang.NonNull ContentNegotiationConfigurer configurer
    ) {
        configurer
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML);
    }

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(
            StandardCharsets.UTF_8
        );
        converter.setWriteAcceptCharset(false);
        return converter;
    }

    @Bean
    public WebServerFactoryCustomizer<
        TomcatServletWebServerFactory
    > tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setURIEncoding(StandardCharsets.UTF_8.name());
                connector.setProperty("relaxedQueryChars", "|{}[]\\");
                connector.setProperty("relaxedPathChars", "|{}[]\\");
                connector.setProperty("rejectIllegalHeader", "false");
                connector.setProperty("allowEncodedSlash", "true");
            });

            factory.addContextCustomizers(context -> {
                context.setRequestCharacterEncoding(
                    StandardCharsets.UTF_8.name()
                );
                context.setResponseCharacterEncoding(
                    StandardCharsets.UTF_8.name()
                );
            });
        };
    }
}
