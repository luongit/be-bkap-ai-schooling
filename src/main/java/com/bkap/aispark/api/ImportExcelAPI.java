package com.bkap.aispark.api;

import com.bkap.aispark.entity.ImportLog;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.service.ImportLogService;
import com.bkap.aispark.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/import")
public class ImportExcelAPI {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ImportLogService importLogService;

    @PostMapping("/students")
    public ResponseEntity<String> importStudents(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("userId") Long userId) {
        int total = 0;
        int success = 0;
        int error = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            // duyệt từ dòng 1 (bỏ header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; // bỏ qua dòng trống
                total++;

                try {
                    Student student = new Student();
                    student.setFullName(getStringCellValue(row, 0));
                    student.setEmail(getStringCellValue(row, 1));
                    student.setPhone(getStringCellValue(row, 2));
                    student.setCode(getStringCellValue(row, 3));
                    student.setClassId(getLongCellValue(row, 4));

                    studentService.addStudent(student);
                    success++;
                } catch (Exception e) {
                    error++;
                    String msg = "Row " + (i + 1) + ": " + e.getClass().getSimpleName() + " - " + e.getMessage();
                    System.out.println(msg);
                    errors.add(msg);
                }
            }

            // lưu log import
            ImportLog log = new ImportLog();
            log.setFileName(file.getOriginalFilename());
            log.setImportedBy(userId);
            log.setTotalRecords(total);
            log.setSuccessCount(success);
            log.setErrorCount(error);

            ObjectMapper mapper = new ObjectMapper();
            String errorJson = mapper.writeValueAsString(errors);
            log.setErrors(errorJson);

            importLogService.saveLog(log);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi đọc file: " + e.getMessage());
        }

        return ResponseEntity.ok("Import xong: " + success + "/" + total + " thành công");
    }

    // ================== helper ==================

    private String getStringCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private Long getLongCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        try {
            return (long) cell.getNumericCellValue();
        } catch (Exception e) {
            cell.setCellType(CellType.STRING);
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty()) return null;
            return Long.parseLong(val);
        }
    }
}
