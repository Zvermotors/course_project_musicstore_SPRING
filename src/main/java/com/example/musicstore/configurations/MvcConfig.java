package com.example.musicstore.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.example.musicstore.services.ProductService.UPLOAD_DIR;


//не используется, создавался для тестирования
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/" + UPLOAD_DIR + "/**")
                .addResourceLocations("file:" + UPLOAD_DIR + "/");
    }
}
