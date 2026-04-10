package com.thinkverge.lms.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
     Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", "dopis40ed",
                "api_key", "947613619259514",
                "api_secret", "XxUqetr9CTTcy4gHluiIRgmRHzk",
                "secure", true
        ));
    }
}