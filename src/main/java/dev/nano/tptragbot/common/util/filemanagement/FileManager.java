package dev.nano.tptragbot.common.util.filemanagement;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
public class FileManager {

    private Path fileStorageLocation;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "txt", "xls", "xlsx", "csv", "json"
    );

    @PostConstruct
    public void init() {
        try {
            // Form the directory path where you will be saving the files.
            fileStorageLocation = Paths.get(System.getProperty("user.dir"), "src/main/resources/uploads").toAbsolutePath().normalize();
            // Create directory at specified location if it does not exist.
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create the directory at the specified location.", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            // Check if the file is empty
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Failed to store empty file.");
            }

            // Check if the file is too large
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Failed to store file, size too large.");
            }

            // Sanitize file name
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

            // Check if the file has an invalid name
            if (originalFileName.contains("..")) {
                throw new IllegalArgumentException("Failed to store file with relative path.");
            }

            // Check if the file has an invalid extension
            String extension = FilenameUtils.getExtension(originalFileName);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("Failed to store file, invalid extension.");
            }

            Path targetLocation = this.fileStorageLocation.resolve(originalFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();

        } catch (IOException e) {
            throw new IllegalArgumentException("Issue occurred while storing the file", e);
        }
    }
}
