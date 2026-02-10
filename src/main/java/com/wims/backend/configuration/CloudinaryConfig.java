package com.wims.backend.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloudname}")
    private String CLOUDINARY_NAME;

    @Value("${cloudinary.key}")
    private String CLOUDINARY_KEY;

    @Value("${cloudinary.secret}")
    private String CLOUDINARY_SECRET;

    @Value("${cloudinary.secure}")
    private String CLOUDINARY_SECURE;

    @Bean
    public Cloudinary cloudinary() {

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUDINARY_NAME,
                "api_key", CLOUDINARY_KEY,
                "api_secret", CLOUDINARY_SECRET,
                "secure", CLOUDINARY_SECURE
        ));
    }
}
