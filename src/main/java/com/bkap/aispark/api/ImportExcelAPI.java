package com.bkap.aispark.api;

import com.bkap.aispark.entity.Classes;
import com.bkap.aispark.entity.ImportLog;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.repository.ClassesRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.service.ImportLogService;
import com.bkap.aispark.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
public class ImportExcelAPI {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ImportLogService importLogService;
    @Autowired
    private ClassesRepository classesRepository;
    @Autowired
    private UserRepository userRepository;

    private final DataFormatter formatter = new DataFormatter();

    @PostMapping("/students")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file,
                                            @RequestParam("userId") Long userId) {
        int total = 0, success = 0, error = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                total++;
                try {
                    Student student = new Student();

                    // 0. Họ tên
                    String fullName = getStringCellValue(row, 0);
                    if (fullName.isBlank()) throw new RuntimeException("Họ tên trống");
                    student.setFullName(fullName);

                    // 1. Lớp
                    String className = getStringCellValue(row, 1);
                    Classes clazz = classesRepository.findByName(className)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp: " + className));
                    student.setClassEntity(clazz);

                    // 2. Username
                    String username = getStringCellValue(row, 2);
                    if (username.isBlank()) throw new RuntimeException("Username trống");
                    if (userRepository.existsByUsername(username)) {
                        throw new RuntimeException("Username đã tồn tại: " + username);
                    }
                    student.setUsername(username);

                    // 3. Email
                    String email = getStringCellValue(row, 3);
                    if (!email.isBlank()) {
                        if (userRepository.existsByEmail(email)) {
                            throw new RuntimeException("Email đã tồn tại: " + email);
                        }
                        student.setEmail(email);
                    } else {
                        student.setEmail(null);
                    }

                    // 4. Mật khẩu (cho phép trống cho thành mật khẩu mặc định)
                    String password = getStringCellValue(row, 4);
                    if (password.isBlank()) password = "123456";
                    student.setDefaultPassword(password);

                    // 5. Số điện thoại
                    String phone = getStringCellValue(row, 5);
                    phone = normalizePhone(phone); // chỉ chuẩn hoá tại đây
                    if (!phone.isBlank()) {
                        if (!phone.matches("^0\\d{9,10}$")) {
                            throw new RuntimeException("Số điện thoại không hợp lệ: " + phone);
                        }
                        if (userRepository.existsByPhone(phone)) {
                            throw new RuntimeException("Số điện thoại đã tồn tại: " + phone);
                        }
                        student.setPhone(phone);
                    } else {
                        student.setPhone(null);
                    }

                    // 6. Ngày sinh
                    LocalDate birthdate = parseDateCell(row, 6);
                    if (birthdate != null && birthdate.isAfter(LocalDate.now())) {
                        throw new RuntimeException("Ngày sinh không hợp lệ: " + birthdate);
                    }
                    student.setBirthdate(birthdate);

                    // 7. Sở thích
                    String hobbyRaw = getStringCellValue(row, 7);
                    String hobbies = "[]";
                    if (!hobbyRaw.isBlank()) {
                        List<String> hobbyList = Arrays.stream(hobbyRaw.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();
                        hobbies = new ObjectMapper().writeValueAsString(hobbyList);
                    }

                    // Lưu
                    studentService.addStudent(student, clazz.getId(), hobbies);
                    success++;

                } catch (Exception e) {
                    error++;
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Lưu log
            ImportLog log = new ImportLog();
            log.setFileName(file.getOriginalFilename());
            log.setImportedBy(userId);
            log.setTotalRecords(total);
            log.setSuccessCount(success);
            log.setErrorCount(error);
            log.setErrors(new ObjectMapper().writeValueAsString(errors));
            importLogService.saveLog(log);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi đọc file: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "total", total,
                "success", success,
                "error", error,
                "errors", errors,
                "message", "Import xong: " + success + "/" + total + " thành công"
        ));
    }

    @PostMapping("/students/validate")
    public ResponseEntity<?> validateStudents(@RequestParam("file") MultipartFile file) {
        int total = 0, valid = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                total++;
                try {
                    // 0) Họ tên
                    String fullName = getStringCellValue(row, 0);
                    if (fullName.isBlank()) throw new RuntimeException("Họ tên trống");

                    // 1) Lớp
                    String className = getStringCellValue(row, 1);
                    classesRepository.findByName(className)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp: " + className));

                    // 2) Username
                    String username = getStringCellValue(row, 2);
                    if (username.isBlank()) throw new RuntimeException("Username trống");
                    if (userRepository.existsByUsername(username)) {
                        throw new RuntimeException("Username đã tồn tại: " + username);
                    }

                    // 3) Email
                    String email = getStringCellValue(row, 3);
                    if (!email.isBlank() && userRepository.existsByEmail(email)) {
                        throw new RuntimeException("Email đã tồn tại: " + email);
                    }

                    // 5) Phone
                    String phone = getStringCellValue(row, 5);
                    phone = normalizePhone(phone);
                    if (!phone.isBlank()) {
                        if (!phone.matches("^0\\d{9,10}$")) {
                            throw new RuntimeException("Số điện thoại không hợp lệ: " + phone);
                        }
                        if (userRepository.existsByPhone(phone)) {
                            throw new RuntimeException("Số điện thoại đã tồn tại: " + phone);
                        }
                    }

                    // 6) Birthdate
                    LocalDate birthdate = parseDateCell(row, 6);
                    if (birthdate != null && birthdate.isAfter(LocalDate.now())) {
                        throw new RuntimeException("Ngày sinh không hợp lệ: " + birthdate);
                    }

                    valid++;
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi đọc file: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "total", total,
                "valid", valid,
                "errorCount", errors.size(),
                "errors", errors
        ));
    }

    // ================== helper ==================

    private String getStringCellValue(Row row, int index) {
        if (row == null) return "";
        Cell cell = row.getCell(index);
        if (cell == null) return "";

       // dd/MM/yyyy
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
        }
        return formatter.formatCellValue(cell).trim();
    }

    /** +84xxx → 0xxx, 84xxx → 0xxx, 9 số → thêm 0 phía trước */
    private String normalizePhone(String raw) {
        if (raw == null) return "";
        String value = raw.trim();
        if (value.isEmpty()) return "";
        value = value.replaceAll("[\\s\\.\\-]", "");
        if (value.startsWith("+84")) {
            value = "0" + value.substring(3);
        } else if (value.startsWith("84") && value.length() > 9) {
            value = "0" + value.substring(2);
        } else if (value.matches("\\d{9}")) {
            value = "0" + value;
        }
        return value;
    }

    private LocalDate parseDateCell(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return DateUtil.getJavaDate(cell.getNumericCellValue())
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty()) return null;
            if (val.matches("\\d+")) {
                double excelSerial = Double.parseDouble(val);
                return DateUtil.getJavaDate(excelSerial)
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            // dd/MM/yyyy
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(val, fmt);
            } catch (Exception e) {
                // yyyy-MM-dd
                try {
                    DateTimeFormatter iso = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    return LocalDate.parse(val, iso);
                } catch (Exception ex) {
                    throw new RuntimeException("Không parse được ngày từ chuỗi: '" + val + "'");
                }
            }
        }
        return null;
    }
}
