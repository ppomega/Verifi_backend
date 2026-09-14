package com.bgv.platform.service;

import com.bgv.platform.exception.FileStorageException;
import com.bgv.platform.exception.InvalidFileException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Handles all interaction with the local filesystem where uploaded
 * documents are physically stored. Files are namespaced per-candidate
 * under the configured upload directory:
 *
 *   {upload-dir}/{candidateId}/{uuid}_{sanitizedOriginalName}
 *
 * Only this service should ever touch the disk directly.
 */
@Service
public class FileStorageService {

    private final Path rootLocation;
    private final List<String> allowedExtensions;
    private final long maxFileSizeBytes;

    public FileStorageService(
            @Value("${bgv.file.upload-dir}") String uploadDir,
            @Value("${bgv.file.allowed-extensions}") String allowedExtensionsCsv,
            @Value("${bgv.file.max-file-size-bytes}") long maxFileSizeBytes) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = Arrays.asList(allowedExtensionsCsv.toLowerCase().split(","));
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize local file storage directory", e);
        }
    }

    public StoredFile store(MultipartFile file, Long candidateId) {
        validate(file);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String storedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        try {
            Path candidateDir = rootLocation.resolve(String.valueOf(candidateId)).normalize();
            if (!candidateDir.startsWith(rootLocation)) {
                throw new FileStorageException("Invalid storage path");
            }
            Files.createDirectories(candidateDir);

            Path targetPath = candidateDir.resolve(storedFileName).normalize();
            if (!targetPath.startsWith(candidateDir)) {
                throw new InvalidFileException("Invalid file name");
            }

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = rootLocation.relativize(targetPath).toString();
            return new StoredFile(originalFilename, storedFileName, relativePath, file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file '" + originalFilename + "' on local disk", e);
        }
    }

    public Resource loadAsResource(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath).normalize();
            if (!filePath.startsWith(rootLocation)) {
                throw new FileStorageException("Invalid file path");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new FileStorageException("File not found on disk: " + relativePath);
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + relativePath, e);
        }
    }

    public void delete(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath).normalize();
            if (!filePath.startsWith(rootLocation)) {
                throw new FileStorageException("Invalid file path");
            }
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file: " + relativePath, e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new InvalidFileException("File exceeds maximum allowed size of " + (maxFileSizeBytes / 1024 / 1024) + "MB");
        }
        String extension = getExtension(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (extension.isEmpty() || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new InvalidFileException("File type not allowed. Allowed types: " + allowedExtensions);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1 || dotIndex == filename.length() - 1) ? "" : filename.substring(dotIndex + 1);
    }

    public record StoredFile(String originalFileName, String storedFileName, String relativePath,
                              long size, String contentType) {
    }
}
