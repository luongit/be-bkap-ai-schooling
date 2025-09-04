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

                String headerCheck = getStringCellValue(row, 5);
                if (headerCheck.equalsIgnoreCase("Ngày sinh")) continue;

                total++;
                try {
                    Student student = new Student();
                    student.setFullName(getStringCellValue(row, 0));
                    student.setPhone(getStringCellValue(row, 1));

                    String className = getStringCellValue(row, 2);
                    Classes clazz = classesRepository.findByName(className)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp: " + className));
                    student.setClassEntity(clazz);

                    student.setUsername(getStringCellValue(row, 3));
                    student.setDefaultPassword(getStringCellValue(row, 4));
                    student.setBirthdate(parseDateCell(row, 5));

                    // hobbies
                    String hobbyRaw = getStringCellValue(row, 6);
                    String hobbies;
                    if (hobbyRaw == null || hobbyRaw.isBlank()) {
                        hobbies = "[]";
                    } else {
                        List<String> hobbyList = Arrays.stream(hobbyRaw.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();
                        hobbies = new ObjectMapper().writeValueAsString(hobbyList);
                    }

                    studentService.addStudent(student, clazz.getId(), hobbies);
                    success++;

                } catch (Exception e) {
                    error++;
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

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

    // ================== helper ==================

    private String getStringCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
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

            // Chuỗi toàn số (ví dụ "37904")
            if (val.matches("\\d+")) {
                double excelSerial = Double.parseDouble(val);
                return DateUtil.getJavaDate(excelSerial)
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            // Thử parse dd/MM/yyyy
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(val, formatter);
            } catch (Exception e) {
                try {
                    DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    return LocalDate.parse(val, isoFormatter);
                } catch (Exception ex) {
                    throw new RuntimeException("Không parse được ngày từ chuỗi: '" + val + "'");
                }
            }
        } else {
            throw new RuntimeException("Kiểu dữ liệu không hỗ trợ: " + cell.getCellType());
        }
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
                    // ====== Full name ======
                    String fullName = getStringCellValue(row, 0);
                    if (fullName.isBlank()) throw new RuntimeException("Họ tên trống");

                    // ====== Phone (có thể trống) ======
                    String phone = getStringCellValue(row, 1);
                    if (!phone.isBlank()) {
                        if (!phone.matches("^[0-9]{9,11}$")) {
                            throw new RuntimeException("Số điện thoại không hợp lệ: " + phone);
                        }
                        if (userRepository.existsByPhone(phone)) {
                            throw new RuntimeException("Số điện thoại đã tồn tại: " + phone);
                        }
                    }

                    // ====== Class ======
                    String className = getStringCellValue(row, 2);
                    classesRepository.findByName(className)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp: " + className));

                    // ====== Username ======
                    String username = getStringCellValue(row, 3);
                    if (username.isBlank()) throw new RuntimeException("Username trống");
                    if (userRepository.existsByUsername(username)) {
                        throw new RuntimeException("Username đã tồn tại: " + username);
                    }

                    // ====== Default password ======
                    String defaultPassword = getStringCellValue(row, 4);
                    if (defaultPassword.isBlank()) throw new RuntimeException("Mật khẩu mặc định trống");

                    // ====== Birthdate ======
                    LocalDate birthdate = parseDateCell(row, 5);
                    if (birthdate != null && birthdate.isAfter(LocalDate.now())) {
                        throw new RuntimeException("Ngày sinh không hợp lệ (lớn hơn hôm nay): " + birthdate);
                    }

                    // ====== Email (có thể trống) ======
//                    String email = getStringCellValue(row, 6);
//                    if (!email.isBlank()) {
//                        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}$")) {
//                            throw new RuntimeException("Email không hợp lệ: " + email);
//                        }
//                        if (userRepository.existsByEmail(email)) {
//                            throw new RuntimeException("Email đã tồn tại: " + email);
//                        }
//                    }

                    valid++;
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi đọc file: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "total", total,
                "valid", valid,
                "errorCount", errors.size(),
                "errors", errors
            ));
    }


}
