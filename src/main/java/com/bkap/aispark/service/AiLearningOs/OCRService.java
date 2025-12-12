package com.bkap.aispark.service.AiLearningOs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OCRService {

    private final ITesseract tesseract;

    public OCRService(
            @Value("${tesseract.datapath}") String dataPath,
            @Value("${tesseract.language:vie+eng}") String language
    ) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(dataPath);   // thư mục cha của folder tessdata
        this.tesseract.setLanguage(language);   // "vie+eng"
        this.tesseract.setOcrEngineMode(1);     // LSTM only
        this.tesseract.setPageSegMode(1);       // Auto page segmentation
    }

   public String extractText(MultipartFile file) throws IOException, TesseractException {
    String filename = file.getOriginalFilename();
    if (filename == null) {
        throw new IllegalArgumentException("File name is null");
    }
    String lowerName = filename.toLowerCase();

    if (lowerName.endsWith(".pdf")) {
        return extractFromPdf(file);
    } else if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")
            || lowerName.endsWith(".bmp") || lowerName.endsWith(".tif") || lowerName.endsWith(".tiff")) {
        return extractFromImage(file);
    } else if (lowerName.endsWith(".docx")) {
        return extractTextFromDocx(file);
    } else {
        throw new IllegalArgumentException("Unsupported file type for OCR: " + file.getOriginalFilename());
    }
}

    private String extractFromImage(MultipartFile file) throws IOException, TesseractException {
        try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IOException("Cannot read image from file");
            }
            return tesseract.doOCR(image);
        }
    }

    private String extractTextFromDocx(MultipartFile file) throws IOException {
    // Sử dụng Apache POI để trích xuất văn bản từ file .docx
    try (InputStream fis = file.getInputStream()) {  // Sử dụng InputStream trực tiếp
        XWPFDocument document = new XWPFDocument(fis);
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);  // Dùng XWPFWordExtractor thay vì XHTMLWordExtractor
        return extractor.getText();
    }
}

    private String extractFromPdf(MultipartFile file) throws IOException, TesseractException {
        StringBuilder sb = new StringBuilder();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int page = 0; page < pageCount; page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String pageText = tesseract.doOCR(image);
                sb.append("\n\n===== PAGE ").append(page + 1).append(" =====\n\n");
                sb.append(pageText);
            }
        }

        return sb.toString();
    }
}
