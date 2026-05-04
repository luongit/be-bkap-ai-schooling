package com.bkap.aispark.service.teach;

import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.LessonFile;
import com.bkap.aispark.entity.teach.enums.LessonFileType;
import com.bkap.aispark.repository.teach.LessonFileRepository;
import com.bkap.aispark.repository.teach.LessonRepository;
import com.bkap.aispark.dto.teach.LessonFileResponse;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class AdminLessonFileService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonFileRepository lessonFileRepository;

    @Value("${upload.lesson-dir:uploads/lessons}")
    private String localLessonUploadDir;

    @Transactional
    public LessonFileResponse uploadLessonMaterial(Long lessonId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn tệp bài giảng cần tải lên!");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Tệp vượt quá 50MB! Vui lòng thử lại.");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại!"));

        String originalName = Objects.requireNonNullElse(
                file.getOriginalFilename(),
                "file_" + System.currentTimeMillis()
        );

        String slugName = toSlugFilename(originalName);
        LessonFileType fileType = detectFileTypeByName(slugName);

        if (fileType == LessonFileType.PDF
                || fileType == LessonFileType.WORD
                || fileType == LessonFileType.HTML) {
            return saveNormalFileLocal(lesson, file, fileType, slugName);
        }

        if (fileType == LessonFileType.ZIP || fileType == LessonFileType.RAR) {
            return extractCompressedAndSaveLocal(lesson, file, fileType, slugName);
        }

        throw new RuntimeException("Không hỗ trợ định dạng tệp vừa tải!");
    }

    private LessonFileResponse saveNormalFileLocal(
            Lesson lesson,
            MultipartFile file,
            LessonFileType fileType,
            String slugName
    ) {
        try {
            Path lessonDir = getLessonDir(lesson.getId());
            Files.createDirectories(lessonDir);

            Path targetLocation = lessonDir.resolve(slugName).normalize();

            if (!targetLocation.startsWith(lessonDir)) {
                throw new IOException("Đường dẫn file không hợp lệ!");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            long size = getPathSize(targetLocation);

            String webPath = "/" + localLessonUploadDir + "/" + lesson.getId() + "/" + slugName;

            LessonFile lessonFile = new LessonFile();
            lessonFile.setLesson(lesson);
            lessonFile.setFileName(slugName);
            lessonFile.setFilePath(webPath);
            lessonFile.setFolderPath(null);
            lessonFile.setFileSize(size);
            lessonFile.setFileType(fileType);
            lessonFile.setIsRoot(false);

            LessonFile saved = lessonFileRepository.save(lessonFile);
            return toFileResponse(saved);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu tài liệu vào server: " + e.getMessage(), e);
        }
    }

    private LessonFileResponse extractCompressedAndSaveLocal(
            Lesson lesson,
            MultipartFile file,
            LessonFileType fileType,
            String slugName
    ) {
        try {
            Path lessonDir = getLessonDir(lesson.getId());
            Files.createDirectories(lessonDir);

            String folderSlug = slugName.replaceAll("(?i)\\.(zip|rar)$", "");
            Path extractDir = lessonDir.resolve(folderSlug).normalize();

            if (!extractDir.startsWith(lessonDir)) {
                throw new IOException("Đường dẫn thư mục giải nén không hợp lệ!");
            }

            if (!Files.exists(extractDir)) {
                Files.createDirectories(extractDir);
            }

            Path compressedPath = lessonDir.resolve(slugName).normalize();

            if (!compressedPath.startsWith(lessonDir)) {
                throw new IOException("Đường dẫn file nén không hợp lệ!");
            }

            Files.copy(file.getInputStream(), compressedPath, StandardCopyOption.REPLACE_EXISTING);

            if (fileType == LessonFileType.ZIP) {
                unzip(compressedPath, extractDir);
            } else if (fileType == LessonFileType.RAR) {
                unrar(compressedPath, extractDir);
            }

            normalizeExtractedFolder(extractDir);
            forceWhiteBackgroundForHtmlPackage(extractDir);

            String folderWebPath = "/" + localLessonUploadDir + "/" + lesson.getId() + "/" + folderSlug;
            long folderSize = getPathSize(extractDir);

            LessonFile lessonFile = new LessonFile();
            lessonFile.setLesson(lesson);
            lessonFile.setFileType(fileType);
            lessonFile.setFileName(folderSlug);
            lessonFile.setFilePath(folderWebPath);
            lessonFile.setFolderPath(folderWebPath);
            lessonFile.setFileSize(folderSize);
            lessonFile.setIsRoot(true);

            LessonFile saved = lessonFileRepository.save(lessonFile);
            return toFileResponse(saved);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi giải nén bài giảng: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String uploadLessonCover(Long lessonId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ảnh bìa!");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại!"));

        try {
            String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "cover.jpg");
            String slugName = "cover_" + System.currentTimeMillis() + "_" + toSlugFilename(originalName);

            Path coverDir = getLessonDir(lesson.getId()).resolve("covers").normalize();
            Files.createDirectories(coverDir);

            Path targetLocation = coverDir.resolve(slugName).normalize();

            if (!targetLocation.startsWith(coverDir)) {
                throw new IOException("Đường dẫn ảnh bìa không hợp lệ!");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String coverUrl = "/" + localLessonUploadDir + "/" + lesson.getId() + "/covers/" + slugName;

            if (lesson.getCoverImage() != null) {
                try {
                    String oldPathStr = lesson.getCoverImage().replaceFirst("^/", "");
                    Files.deleteIfExists(Paths.get(oldPathStr).toAbsolutePath().normalize());
                } catch (Exception ignored) {
                }
            }

            lesson.setCoverImage(coverUrl);
            lessonRepository.save(lesson);

            return coverUrl;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload ảnh bìa: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteLessonFile(Long fileId) {
        LessonFile file = lessonFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File không tồn tại!"));

        Long lessonId = file.getLesson().getId();

        try {
            Path lessonDir = getLessonDir(lessonId);

            if (file.getFileType() == LessonFileType.PDF
                    || file.getFileType() == LessonFileType.WORD
                    || file.getFileType() == LessonFileType.HTML) {

                Path filePath = lessonDir.resolve(file.getFileName()).normalize();

                if (filePath.startsWith(lessonDir)) {
                    Files.deleteIfExists(filePath);
                }

            } else if (file.getFileType() == LessonFileType.ZIP || file.getFileType() == LessonFileType.RAR) {

                Path folderPath = lessonDir.resolve(file.getFileName()).normalize();

                if (folderPath.startsWith(lessonDir)) {
                    deleteDirectoryRecursively(folderPath);
                }

                String ext = file.getFileType() == LessonFileType.ZIP ? ".zip" : ".rar";
                Path compressedPath = lessonDir.resolve(file.getFileName() + ext).normalize();

                if (compressedPath.startsWith(lessonDir)) {
                    Files.deleteIfExists(compressedPath);
                }
            }

        } catch (Exception e) {
            System.out.println("Không xóa được file/thư mục local: " + e.getMessage());
        }

        lessonFileRepository.delete(file);
    }

    private Path getLessonDir(Long lessonId) {
        return Paths.get(localLessonUploadDir, lessonId.toString())
                .toAbsolutePath()
                .normalize();
    }

    private LessonFileType detectFileTypeByName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".pdf")) return LessonFileType.PDF;
        if (lower.endsWith(".zip")) return LessonFileType.ZIP;
        if (lower.endsWith(".rar")) return LessonFileType.RAR;
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return LessonFileType.HTML;
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return LessonFileType.WORD;

        throw new RuntimeException("Không hỗ trợ định dạng tệp vừa tải!");
    }

    private void unzip(Path zipFile, Path targetDir) throws IOException {
        try {
            unzipWithCharset(zipFile, targetDir, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Không giải nén ZIP bằng UTF-8 được, thử CP437: " + e.getMessage());
            resetDirectory(targetDir);

            try {
                unzipWithCharset(zipFile, targetDir, Charset.forName("CP437"));
            } catch (IllegalArgumentException ex) {
                System.err.println("⚠️ Không giải nén ZIP bằng CP437 được, thử windows-1258: " + ex.getMessage());
                resetDirectory(targetDir);
                unzipWithCharset(zipFile, targetDir, Charset.forName("windows-1258"));
            }
        }
    }

    private void unzipWithCharset(Path zipFile, Path targetDir, Charset charset) throws IOException {
        Path safeTargetDir = targetDir.toAbsolutePath().normalize();
        Files.createDirectories(safeTargetDir);

        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile)),
                charset
        )) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                if (entryName == null || entryName.trim().isEmpty()) {
                    zis.closeEntry();
                    continue;
                }

                entryName = entryName.replace("\\", "/").trim();

                Path newPath = safeTargetDir.resolve(entryName).normalize();

                if (!newPath.startsWith(safeTargetDir)) {
                    throw new IOException("Zip Slip detected: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) {
                        Files.createDirectories(newPath.getParent());
                    }

                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }

                zis.closeEntry();
            }
        }
    }

    private void unrar(Path rarFile, Path targetDir) throws IOException {
        Path safeTargetDir = targetDir.toAbsolutePath().normalize();
        Files.createDirectories(safeTargetDir);

        try (Archive archive = new Archive(rarFile.toFile())) {
            FileHeader header;

            while ((header = archive.nextFileHeader()) != null) {
                String rawName = header.isUnicode()
                        ? header.getFileNameW()
                        : header.getFileNameString();

                if (rawName == null || rawName.trim().isEmpty()) {
                    continue;
                }

                rawName = rawName.replace("\\", "/").trim();

                Path outPath = safeTargetDir.resolve(rawName).normalize();

                if (!outPath.startsWith(safeTargetDir)) {
                    throw new IOException("Rar Slip detected: " + rawName);
                }

                if (header.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    if (outPath.getParent() != null) {
                        Files.createDirectories(outPath.getParent());
                    }

                    try (OutputStream os = Files.newOutputStream(outPath)) {
                        archive.extractFile(header, os);
                    }
                }
            }

        } catch (RarException e) {
            throw new IOException("Lỗi giải nén RAR", e);
        }
    }

    private void normalizeExtractedFolder(Path extractDir) throws IOException {
        Path indexFile;

        try (var stream = Files.walk(extractDir, 10)) {
            indexFile = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("index.html"))
                    .findFirst()
                    .orElse(null);
        }

        if (indexFile == null) {
            System.err.println("⚠️ Không tìm thấy index.html trong package, bỏ qua normalize folder: " + extractDir);
            return;
        }

        Path indexRoot = indexFile.getParent();

        if (indexRoot == null || indexRoot.equals(extractDir)) {
            return;
        }

        try (var stream = Files.list(indexRoot)) {
            for (Path source : stream.toList()) {
                Path target = extractDir.resolve(source.getFileName()).normalize();

                if (!target.startsWith(extractDir)) {
                    throw new IOException("Đường dẫn normalize không hợp lệ!");
                }

                if (source.equals(target)) {
                    continue;
                }

                moveContent(source, target);
            }
        }

        cleanupEmptyDirs(extractDir);
    }

    private void moveContent(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            if (!Files.exists(target)) {
                Files.createDirectories(target);
            }

            try (var stream = Files.list(source)) {
                for (Path child : stream.toList()) {
                    moveContent(child, target.resolve(child.getFileName()).normalize());
                }
            }

            deleteDirectoryRecursively(source);

        } else {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void forceWhiteBackgroundForHtmlPackage(Path extractDir) {
        try (var stream = Files.walk(extractDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);

                try {
                    if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                        injectWhiteBackgroundIntoHtml(path);
                    }

                    if (fileName.endsWith(".css")) {
                        appendWhiteBackgroundIntoCss(path);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Không ép được nền trắng cho: " + path + " | " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("⚠️ Không scan được package HTML5: " + e.getMessage());
        }
    }

    private void injectWhiteBackgroundIntoHtml(Path htmlPath) throws IOException {
        String content = readTextFileSafe(htmlPath);

        String customCss = """
        <style id="lms-force-white-background">
            html,
            body,
            .pageViewer {
                background: #ffffff !important;
                background-color: #ffffff !important;
            }
        </style>
        <script id="lms-force-white-background-script">
            (function () {
                function forceWhitePageViewer() {
                    var els = document.querySelectorAll('.pageViewer');
                    els.forEach(function(el) {
                        el.style.setProperty('background', '#ffffff', 'important');
                        el.style.setProperty('background-color', '#ffffff', 'important');
                    });
                }

                setTimeout(forceWhitePageViewer, 100);
                setTimeout(forceWhitePageViewer, 500);
                setTimeout(forceWhitePageViewer, 1000);
                setTimeout(forceWhitePageViewer, 2000);

                window.addEventListener('load', forceWhitePageViewer);

                if (window.MutationObserver) {
                    var observer = new MutationObserver(forceWhitePageViewer);
                    window.addEventListener('load', function () {
                        if (document.body) {
                            observer.observe(document.body, {
                                childList: true,
                                subtree: true,
                                attributes: true,
                                attributeFilter: ['style', 'class']
                            });
                        }
                    });
                }
            })();
        </script>
        """;

        content = removeOldWhiteBackgroundInjection(content);

        if (content.toLowerCase(Locale.ROOT).contains("</head>")) {
            content = content.replaceFirst("(?i)</head>", customCss + "</head>");
        } else {
            content = customCss + content;
        }

        Files.writeString(htmlPath, content, StandardCharsets.UTF_8);
    }

    private void appendWhiteBackgroundIntoCss(Path cssPath) throws IOException {
        String content = readTextFileSafe(cssPath);

        content = content.replaceAll(
                "(?s)/\\* lms-force-white-background-css-start \\*/.*?/\\* lms-force-white-background-css-end \\*/",
                ""
        );

        String customCss = """

        /* lms-force-white-background-css-start */
        html,
        body {
            background: #ffffff !important;
            background-color: #ffffff !important;
        }
        /* lms-force-white-background-css-end */
        """;

        Files.writeString(cssPath, content + customCss, StandardCharsets.UTF_8);
    }

    private String removeOldWhiteBackgroundInjection(String content) {
        content = content.replaceAll(
                "(?is)<style[^>]*id=[\"']lms-force-white-background[\"'][^>]*>.*?</style>",
                ""
        );

        content = content.replaceAll(
                "(?is)<script[^>]*id=[\"']lms-force-white-background-script[\"'][^>]*>.*?</script>",
                ""
        );

        return content;
    }

    private String readTextFileSafe(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);

        try {
            return StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return Charset.forName("windows-1258")
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        }
    }

    private void resetDirectory(Path dir) throws IOException {
        deleteDirectoryRecursively(dir);
        Files.createDirectories(dir);
    }

    private void cleanupEmptyDirs(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    if (Files.isDirectory(p) && !p.equals(root)) {
                        try (var children = Files.list(p)) {
                            if (children.findAny().isEmpty()) {
                                Files.deleteIfExists(p);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("⚠️ Không xóa được thư mục rỗng: " + p + " | " + e.getMessage());
                }
            });
        }
    }

    private void deleteDirectoryRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }

        try (var stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new RuntimeException("Không xóa được: " + p + " | " + e.getMessage(), e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }

            throw e;
        }
    }

    private long getPathSize(Path path) {
        try {
            if (!Files.exists(path)) {
                return 0L;
            }

            if (Files.isRegularFile(path)) {
                return Files.size(path);
            }

            try (var stream = Files.walk(path)) {
                return stream
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0L;
                            }
                        })
                        .sum();
            }
        } catch (Exception e) {
            return 0L;
        }
    }

    private String toSlugFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file_" + System.currentTimeMillis();
        }

        String name = filename.trim();

        int dotIndex = name.lastIndexOf(".");
        String baseName = dotIndex > 0 ? name.substring(0, dotIndex) : name;
        String extension = dotIndex > 0 ? name.substring(dotIndex).toLowerCase(Locale.ROOT) : "";

        String normalized = Normalizer.normalize(baseName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        normalized = normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (normalized.isBlank()) {
            normalized = "file_" + System.currentTimeMillis();
        }

        return normalized + extension;
    }

    private LessonFileResponse toFileResponse(LessonFile f) {
        LessonFileResponse r = new LessonFileResponse();

        r.setId(f.getId());

        if (f.getFileType() != null) {
            r.setFileType(f.getFileType().name());
        }

        r.setFileName(f.getFileName());
        r.setFilePath(f.getFilePath());
        r.setFolderPath(f.getFolderPath());
        r.setFileSize(f.getFileSize());
        r.setIsRoot(f.getIsRoot());

        return r;
    }
}