package com.wims.backend.controller;

import com.wims.backend.service.feature.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor // Dùng Lombok cho chuẩn Clean Code
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Gọi hàm uploadImage mới (đã viết ở FileStorageService)
            // Hàm này trả về thẳng link https://res.cloudinary.com/...
            String imageUrl = fileStorageService.uploadImage(file);

            // Trả về luôn đường dẫn ảnh trên Cloud
            return ResponseEntity.ok(imageUrl);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi upload: " + e.getMessage());
        }
    }
}