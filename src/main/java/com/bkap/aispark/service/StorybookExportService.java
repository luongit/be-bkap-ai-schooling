package com.bkap.aispark.service;

import com.bkap.aispark.entity.Storybook;
import com.bkap.aispark.entity.StorybookExport;
import com.bkap.aispark.entity.StorybookPage;
import com.bkap.aispark.repository.StorybookExportRepository;
import com.bkap.aispark.repository.StorybookPageRepository;
import com.bkap.aispark.repository.StorybookRepository;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream; 
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorybookExportService {

    private final StorybookRepository storybookRepository;
    private final StorybookPageRepository pageRepository;
    private final StorybookExportRepository exportRepository;
    private final R2StorageService r2StorageService;

    public String exportPdf(Long storybookId) {

        Storybook storybook = storybookRepository.findById(storybookId)
                .orElseThrow(() -> new RuntimeException("Storybook not found"));

        List<StorybookPage> pages =
                pageRepository.findByStorybookIdOrderByPageNumberAsc(storybookId);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document doc = new Document(PageSize.A4, 36, 36, 48, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

         
            // 1. Đọc file font từ thư mục src/main/resources/fonts/
            InputStream fontStream = getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf");
            if (fontStream == null) {
                throw new RuntimeException("Font file không tìm thấy trong resources/fonts/Roboto-Regular.ttf");
            }
            byte[] fontBytes = fontStream.readAllBytes();

            // 2. Tạo BaseFont từ mảng byte để nhúng vào PDF
            BaseFont bf = BaseFont.createFont(
                    "Roboto-Regular.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontBytes,
                    null
            );

            Font titleFont = new Font(bf, 20, Font.BOLD);
            Font descFont  = new Font(bf, 12);
            Font textFont  = new Font(bf, 13);

            /* ===== TITLE ===== */
            Paragraph title = new Paragraph(storybook.getTitle(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(Chunk.NEWLINE);

            if (storybook.getDescription() != null) {
                Paragraph desc = new Paragraph(storybook.getDescription(), descFont);
                desc.setAlignment(Element.ALIGN_CENTER);
                doc.add(desc);
                doc.add(Chunk.NEWLINE);
            }

            /* ===== PAGES ===== */
            for (int i = 0; i < pages.size(); i++) {
                StorybookPage page = pages.get(i);

                // Image
                if (page.getImageUrl() != null && !page.getImageUrl().isBlank()) {
                    try {
                        Image img = Image.getInstance(page.getImageUrl());
                        img.scaleToFit(420, 420);
                        img.setAlignment(Element.ALIGN_CENTER);
                        doc.add(img);
                        doc.add(Chunk.NEWLINE);
                    } catch (Exception ex) {
                        doc.add(new Paragraph("[Không tải được ảnh]", descFont));
                        doc.add(Chunk.NEWLINE);
                    }
                }

                // Text
                if (page.getTextContent() != null && !page.getTextContent().isBlank()) {
                    Paragraph text = new Paragraph(page.getTextContent(), textFont);
                    text.setLeading(0, 1.6f);
                    doc.add(text);
                }

                if (i < pages.size() - 1) {
                    doc.newPage();
                }
            }

            doc.close();

            byte[] pdfBytes = out.toByteArray();

            /* ===== Upload R2 ===== */
            String key = "storybook/exports/" + storybookId + "/storybook.pdf";

            String pdfUrl = r2StorageService.uploadBytes(
                    pdfBytes,
                    key,
                    "application/pdf"
            );

            /* ===== Save DB ===== */
            exportRepository.save(
                    StorybookExport.builder()
                            .storybookId(storybookId)
                            .exportType("PDF")
                            .fileUrl(pdfUrl)
                            .build()
            );

            return pdfUrl;

        } catch (Exception e) {
            // Log lỗi chi tiết ra console để dễ debug
            e.printStackTrace(); 
            throw new RuntimeException("Export PDF failed: " + e.getMessage(), e);
        }
    }
}