package com.wims.backend.service.feature;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        // 1. Upload file lên Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "wims_product_images"
        ));

        // 2. Lấy đường dẫn ảnh (URL) trả về
        return uploadResult.get("secure_url").toString();
    }

    public void deleteImage(String imageUrl) throws IOException {
        // 1. Lấy Public ID từ URL (Cái này hơi tricky vì phải cắt chuỗi)
        String publicId = getPublicIdFromUrl(imageUrl);

        // 2. Gọi lệnh xóa của Cloudinary
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    // Hàm phụ trợ: Trích xuất Public ID từ URL
    // URL: https://res.../upload/v1234/wims_product_images/abc.png
    // Output: wims_product_images/abc
    private String getPublicIdFromUrl(String url) {
        try {
            // Cắt chuỗi dựa vào folder name "wims_product_images"
            int startIndex = url.indexOf("wims_product_images");
            if (startIndex == -1) return null;

            // Lấy phần đuôi: wims_product_images/abc.png
            String publicIdWithExtension = url.substring(startIndex);

            // Bỏ phần đuôi mở rộng (.png, .jpg) để lấy ID chuẩn
            int lastDotIndex = publicIdWithExtension.lastIndexOf(".");
            return publicIdWithExtension.substring(0, lastDotIndex);
        } catch (Exception e) {
            return null; // Không lấy được ID thì thôi, bỏ qua
        }
    }
}