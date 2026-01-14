package com.wims.backend.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dfdyfg6bd",
                "api_key", "374153475555556",
                "api_secret", "SUfU3BPGWnPM-B6AT_6aNWZvcUU",
                "secure", true
        ));
    }
}
