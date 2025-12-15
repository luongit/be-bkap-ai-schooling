package com.bkap.aispark.service;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    // Giữ nguyên như bạn muốn — không đưa vào config
    private final String publicBaseUrl = "https://pub-73166ad3cfdb4cacae1a32467e7110c0.r2.dev";

    private S3Client buildClient() {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.US_EAST_1)
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(true).build()
                )
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build();
    }

    /* ======================================================
            UPLOAD (Giữ nguyên code của bạn)
    ====================================================== */
    public String uploadFile(MultipartFile file) throws IOException {
        String cleanName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String fileName = UUID.randomUUID() + "_" + cleanName;

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            if (fileName.endsWith(".mp4")) contentType = "video/mp4";
            else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fileName.endsWith(".png")) contentType = "image/png";
            else contentType = "application/octet-stream";
        }

        S3Client s3 = buildClient();

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        return publicBaseUrl + "/" + fileName;
    }

    /* ======================================================
            DELETE FILE – BẠN CHƯA CÓ! (Thêm mới)
    ====================================================== */
    public void deleteFile(String fileUrl) {

        if (fileUrl == null || fileUrl.isBlank()) return;
        if (!fileUrl.contains(publicBaseUrl)) return;

        // Tách tên file từ public URL
        String key = fileUrl.replace(publicBaseUrl + "/", "");

        try {
            S3Client s3 = buildClient();

            s3.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );

            System.out.println("🗑 Đã xoá file khỏi R2: " + key);

        } catch (Exception e) {
            System.err.println("❌ Lỗi xoá file R2: " + e.getMessage());
        }
    }
}
