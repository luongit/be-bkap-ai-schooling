package com.bkap.aispark.service;

import com.bkap.aispark.entity.Storybook;
import com.bkap.aispark.entity.StorybookExport;
import com.bkap.aispark.entity.StorybookPage;
import com.bkap.aispark.repository.StorybookExportRepository;
import com.bkap.aispark.repository.StorybookPageRepository;
import com.bkap.aispark.repository.StorybookRepository;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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

            /* ===== FONT (hỗ trợ tiếng Việt) ===== */
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font descFont  = new Font(Font.HELVETICA, 12);
            Font textFont  = new Font(Font.HELVETICA, 13);



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
                        // Không cho export fail chỉ vì ảnh lỗi
                        doc.add(new Paragraph("[Không tải được ảnh]", descFont));
                    }
                }

                // Text
                if (page.getTextContent() != null && !page.getTextContent().isBlank()) {
                    Paragraph text = new Paragraph(page.getTextContent(), textFont);
                    text.setLeading(0, 1.6f);
                    doc.add(text);
                }

                // New page trừ trang cuối
                if (i < pages.size() - 1) {
                    doc.newPage();
                }
            }

            doc.close();

            byte[] pdfBytes = out.toByteArray();

            /* ===== Upload lên R2 ===== */
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
            throw new RuntimeException("Export PDF failed", e);
        }
    }
}
