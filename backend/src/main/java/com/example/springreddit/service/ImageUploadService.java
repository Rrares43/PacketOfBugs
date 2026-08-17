package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

/**
 * Service responsible for uploading images to AWS S3, generating pre-signed URLs,
 * and triggering asynchronous image filtering.
 */
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private final S3Client s3Client;
    private final ImageEditService imageEditService;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;


    /**
     * Uploads an image file to AWS S3 and optionally triggers an asynchronous editing task
     * if a valid filter ID is provided.
     *
     * @param file     the multipart image file to upload
     * @param filterId the optional filter ID to apply to the image
     * @return the public URL of the uploaded image
     */
    public String upload(MultipartFile file, Integer filterId) {
        LOGGER.info("Starting image upload process. Original filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        String extension = getExtension(file);
        try {
            return upload(file.getInputStream(), file.getSize(), file.getContentType(), extension, filterId);
        } catch (IOException e) {
            LOGGER.error("IO error during file upload processing: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read upload file stream.", e);
        }
    }

    /**
     * Uploads image bytes already processed in memory. This keeps image optimization
     * and S3 storage decoupled while preserving the existing optional filter flow.
     */
    public String upload(InputStream inputStream, long contentLength, String contentType,
                         String extension, Integer filterId) {
        if (inputStream == null || contentLength <= 0) {
            throw new IllegalArgumentException("Image stream is empty.");
        }

        Integer validFilterId = imageEditService.getValidFilterId(filterId);
        String safeExtension = extension == null || extension.isBlank() ? ".jpg" : extension;
        String key = "images/" + UUID.randomUUID() + safeExtension;

        try {
            uploadStream(inputStream, contentLength, key, contentType);
            String finalUrl = buildPublicUrl(key);
            LOGGER.info("Image successfully uploaded to S3 with key: {}", key);

            if (validFilterId == null) {
                return finalUrl;
            }

            String downloadUrl = generateDownloadUrl(key);
            String uploadUrl = generateUploadUrl(key);

            try {
                imageEditService.edit(downloadUrl, uploadUrl, validFilterId);
            } catch (Exception e) {
                LOGGER.error("Async image editing failed for key {}: {}", key, e.getMessage(), e);
            }

            return finalUrl;

        } catch (S3Exception e) {
            LOGGER.error("AWS S3 error during upload for key {}: {}", key, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload image to cloud storage.", e);
        }

    }

    /**
     * Uploads an input stream to AWS S3 using the provided content type and key.
     *
     * @param inputStream   the input stream of the file
     * @param contentLength the size of the content in bytes
     * @param key           the destination S3 object key
     * @param contentType   the MIME type of the file
     */
    private void uploadStream(InputStream inputStream, long contentLength, String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

    }

    /**
     * Generates a pre-signed download URL for a specific S3 object.
     *
     * @param key the S3 object key
     * @return the pre-signed download URL as a String
     */
    private String generateDownloadUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Generates a pre-signed upload URL for a specific S3 object.
     *
     * @param key the S3 object key
     * @return the pre-signed upload URL as a String
     */
    private String generateUploadUrl(String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    /**
     * Builds the public HTTP URL for an S3 object based on the bucket and region configuration.
     *
     * @param key the S3 object key
     * @return the public URL string
     */
    private String buildPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    private static @NonNull String getExtension(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }

        String originalFilename = file.getOriginalFilename();

        return originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
    }
}