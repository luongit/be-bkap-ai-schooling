package com.bkap.aispark.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class R2StorageService {

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    public String uploadFile(MultipartFile file) throws IOException {
        String cleanName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String fileName = UUID.randomUUID() + "_" + cleanName;

        // Xác định content-type an toàn
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            if (fileName.endsWith(".mp4")) contentType = "video/mp4";
            else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fileName.endsWith(".png")) contentType = "image/png";
            else contentType = "application/octet-stream";
        }

        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);

        // Kết nối tới Cloudflare R2 qua S3 API
        S3Client s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build();

        // Upload file
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        // ✅ Public URL (dùng để người xem tải file)
        String publicBaseUrl = "https://pub-73166ad3cfdb4cacae1a32467e7110c0.r2.dev";

        // Trả về link public để người dùng có thể xem trực tiếp
        return publicBaseUrl + "/" + fileName;
    }
}
