package com.bkap.aispark.service.teach;

import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.LessonFile;
import com.bkap.aispark.entity.teach.enums.LessonFileType;
import com.bkap.aispark.repository.teach.LessonFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LessonExtractionService {

    @Autowired
    private LessonFileRepository lessonFileRepository;

    @Autowired
    private ExtractionProgressService progressService;

    @Async
    @Transactional
    public void processExtractionAsync(
            Lesson lesson, 
            Path compressedPath, 
            Path extractDir, 
            LessonFileType fileType, 
            String folderSlug, 
            String folderWebPath,
            AdminLessonFileService mainService,
            Long zipFileId // Nhận ID để xóa rác
    ) {
        Long lessonId = lesson.getId();
        try {
            progressService.updateProgress(lessonId, 1);

            // 1. Giải nén
            if (fileType == LessonFileType.ZIP) {
                mainService.unzip(compressedPath, extractDir, lessonId, progressService); 
            } else if (fileType == LessonFileType.RAR) {
                mainService.unrar(compressedPath, extractDir); 
                progressService.updateProgress(lessonId, 50);
            }

            // 2. Tối ưu folder và tiêm CSS
            mainService.normalizeExtractedFolder(extractDir);
            mainService.forceWhiteBackgroundForHtmlPackage(extractDir);
            progressService.updateProgress(lessonId, 95);

            // 3. Lưu thông tin thư mục đã giải nén (isRoot = true)
            long folderSize = mainService.getPathSize(extractDir);
            LessonFile folderFile = new LessonFile();
            folderFile.setLesson(lesson);
            folderFile.setFileType(fileType);
            folderFile.setFileName(folderSlug);
            folderFile.setFilePath(folderWebPath);
            folderFile.setFolderPath(folderWebPath);
            folderFile.setFileSize(folderSize);
            folderFile.setIsRoot(true);
            lessonFileRepository.save(folderFile);
            

            try {
                // Xóa file .zip/.rar vật lý trên VPS
                Files.deleteIfExists(compressedPath); 
                
                // Xóa bản ghi file ZIP trong Database
                if (zipFileId != null) {
                    lessonFileRepository.deleteById(zipFileId); 
                }
                System.out.println("🗑️ Đã xóa file ZIP gốc để giải phóng dung lượng VPS!");
            } catch (Exception ex) {
                System.err.println("⚠️ Lỗi khi xóa rác: " + ex.getMessage());
            }

            progressService.updateProgress(lessonId, 100);
            Thread.sleep(2000);
            progressService.finishProgress(lessonId);

        } catch (Exception e) {
            e.printStackTrace();
            progressService.updateProgress(lessonId, -2); 
        }
    }
}