package com.bkap.aispark.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class FakeMultipartFile implements MultipartFile {

    private final String fileName;
    private final String contentType;
    private final byte[] content;

    public FakeMultipartFile(String fileName, String contentType, byte[] content) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
    }
}
