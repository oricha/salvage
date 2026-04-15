package com.cardealer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileUploadUtil {

    public static final int MIN_IMAGE_COUNT = 20;
    public static final int MAX_IMAGE_COUNT = 25;
    public static final int MIN_WIDTH = 800;
    public static final int MIN_HEIGHT = 600;
    public static final int GRID_THUMBNAIL_WIDTH = 150;
    public static final int GRID_THUMBNAIL_HEIGHT = 150;
    public static final int LIST_THUMBNAIL_WIDTH = 400;
    public static final int LIST_THUMBNAIL_HEIGHT = 300;

    @Value("${file.upload-dir:uploads/cars}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
        ".jpg", ".jpeg", ".png", ".webp"
    );

    /**
     * Save a single file
     */
    public String saveFile(MultipartFile file) throws IOException {
        log.info("Saving file: {}", file.getOriginalFilename());
        
        // Validate file
        validateFile(file);
        
        // Generate unique filename
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath);
        }
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        generateThumbnail(fileName, GRID_THUMBNAIL_WIDTH, GRID_THUMBNAIL_HEIGHT);
        generateThumbnail(fileName, LIST_THUMBNAIL_WIDTH, LIST_THUMBNAIL_HEIGHT);
        
        log.info("File saved successfully: {}", fileName);
        return fileName;
    }

    /**
     * Save multiple files
     */
    public List<String> saveFiles(List<MultipartFile> files) throws IOException {
        log.info("Saving {} files", files.size());
        validateImageCount(files);
        
        List<String> savedFileNames = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileName = saveFile(file);
                savedFileNames.add(fileName);
            }
        }
        
        log.info("Saved {} files successfully", savedFileNames.size());
        return savedFileNames;
    }

    /**
     * Delete a file
     */
    public void deleteFile(String fileName) throws IOException {
        log.info("Deleting file: {}", fileName);
        
        if (fileName == null || fileName.isEmpty()) {
            log.warn("Attempted to delete file with null or empty filename");
            return;
        }
        
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        
        if (Files.exists(filePath)) {
            Files.deleteIfExists(filePath);
            Files.deleteIfExists(getThumbnailPath(fileName, GRID_THUMBNAIL_WIDTH, GRID_THUMBNAIL_HEIGHT));
            Files.deleteIfExists(getThumbnailPath(fileName, LIST_THUMBNAIL_WIDTH, LIST_THUMBNAIL_HEIGHT));
            log.info("File deleted successfully: {}", fileName);
        } else {
            log.warn("File not found for deletion: {}", fileName);
        }
    }

    /**
     * Delete multiple files
     */
    public void deleteFiles(List<String> fileNames) {
        log.info("Deleting {} files", fileNames.size());
        
        for (String fileName : fileNames) {
            try {
                deleteFile(fileName);
            } catch (IOException e) {
                log.error("Error deleting file: {}", fileName, e);
            }
        }
    }

    /**
     * Validate file
     */
    public void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("El archivo es demasiado grande. Tamaño máximo: %d MB", 
                    MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (!isValidContentType(contentType)) {
            throw new IllegalArgumentException(
                "Tipo de archivo no válido. Solo se permiten imágenes JPEG, PNG y WebP"
            );
        }
        
        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidExtension(originalFilename)) {
            throw new IllegalArgumentException(
                "Extensión de archivo no válida. Solo se permiten: " + ALLOWED_EXTENSIONS
            );
        }

        validateImageDimensions(file);
        
        log.debug("File validation passed for: {}", originalFilename);
    }

    public void validateImageCount(List<MultipartFile> files) {
        long validFileCount = files == null ? 0 : files.stream()
            .filter(file -> file != null && !file.isEmpty())
            .count();

        if (validFileCount < MIN_IMAGE_COUNT || validFileCount > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException(
                String.format("Debe subir entre %d y %d imágenes por vehículo", MIN_IMAGE_COUNT, MAX_IMAGE_COUNT)
            );
        }
    }

    public void validateImageDimensions(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("No se pudo leer la imagen subida");
            }
            if (image.getWidth() < MIN_WIDTH || image.getHeight() < MIN_HEIGHT) {
                throw new IllegalArgumentException(
                    String.format("La imagen debe tener una resolución mínima de %dx%d píxeles", MIN_WIDTH, MIN_HEIGHT)
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo procesar la imagen subida", e);
        }
    }

    public String generateThumbnail(String originalFileName, int width, int height) throws IOException {
        Path originalPath = getFilePath(originalFileName);
        if (!Files.exists(originalPath)) {
            throw new IllegalArgumentException("No existe la imagen original para generar miniatura: " + originalFileName);
        }

        Path thumbnailsDir = Paths.get(uploadDir).resolve("thumbnails");
        Files.createDirectories(thumbnailsDir);

        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen original: " + originalFileName);
        }

        BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnail.createGraphics();
        graphics.drawImage(
            originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH),
            0,
            0,
            width,
            height,
            null
        );
        graphics.dispose();

        String thumbnailFileName = getThumbnailFileName(originalFileName, width, height);
        Path thumbnailPath = thumbnailsDir.resolve(thumbnailFileName);
        ImageIO.write(thumbnail, "jpg", thumbnailPath.toFile());
        return "thumbnails/" + thumbnailFileName;
    }

    public Path getThumbnailPath(String fileName, int width, int height) {
        return Paths.get(uploadDir).resolve("thumbnails").resolve(getThumbnailFileName(fileName, width, height));
    }

    public String getThumbnailFileName(String originalFileName, int width, int height) {
        String cleanFilename = StringUtils.cleanPath(originalFileName);
        int lastDotIndex = cleanFilename.lastIndexOf('.');
        String baseName = lastDotIndex > 0 ? cleanFilename.substring(0, lastDotIndex) : cleanFilename;
        return baseName + "_" + width + "x" + height + ".jpg";
    }

    /**
     * Check if content type is valid
     */
    private boolean isValidContentType(String contentType) {
        return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    /**
     * Check if file extension is valid
     */
    private boolean isValidExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream()
            .anyMatch(lowerFilename::endsWith);
    }

    /**
     * Generate unique filename
     */
    private String generateUniqueFileName(String originalFilename) {
        String cleanFilename = StringUtils.cleanPath(originalFilename);
        String extension = "";
        
        int lastDotIndex = cleanFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = cleanFilename.substring(lastDotIndex);
        }
        
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + extension;
    }

    /**
     * Get upload directory path
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Get full file path
     */
    public Path getFilePath(String fileName) {
        return Paths.get(uploadDir).resolve(fileName);
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        Path filePath = getFilePath(fileName);
        return Files.exists(filePath);
    }
}
