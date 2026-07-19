package vn.anyen.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public String upload(MultipartFile file) throws IOException {
        return upload(file, "general");
    }

    public String upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File tải lên đang rỗng");
        }

        if (isCloudinaryConfigured()) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "anyen/" + sanitizeFolder(folder),
                                "resource_type", "image"
                        )
                );

                Object secureUrl = result.get("secure_url");
                if (secureUrl != null) {
                    return secureUrl.toString();
                }
            } catch (Exception exception) {
                LOGGER.warn(
                        "Không thể upload Cloudinary, chuyển sang lưu local: {}",
                        exception.getMessage()
                );
            }
        }

        return saveLocal(file, folder);
    }

    private String saveLocal(MultipartFile file, String folder) throws IOException {
        String safeFolder = sanitizeFolder(folder);
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();
        Path folderPath = root.resolve(safeFolder).normalize();

        if (!folderPath.startsWith(root)) {
            throw new IOException("Thư mục upload không hợp lệ");
        }

        Files.createDirectories(folderPath);

        String extension = resolveExtension(file);
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path destination = folderPath.resolve(filename).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        String baseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + "/uploads/" + safeFolder + "/" + filename;
    }

    private boolean isCloudinaryConfigured() {
        return notBlank(cloudName) && notBlank(apiKey) && notBlank(apiSecret);
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String sanitizeFolder(String folder) {
        String value = folder == null ? "general" : folder.trim();
        value = value.replaceAll("[^a-zA-Z0-9_-]", "");
        return value.isEmpty() ? "general" : value;
    }

    private String resolveExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) return ".png";
        if ("image/webp".equalsIgnoreCase(contentType)) return ".webp";
        if ("image/gif".equalsIgnoreCase(contentType)) return ".gif";
        return ".jpg";
    }
}
