package com.bkap.aispark.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.ClassesDTO;
import com.bkap.aispark.entity.Classes;
import com.bkap.aispark.entity.Schools;
import com.bkap.aispark.repository.ClassesRepository;
import com.bkap.aispark.repository.SchoolsRepository;

@RestController
@RequestMapping("api/class")
public class ClassApi {

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private SchoolsRepository schoolsRepository;

    // Xem danh sách: Ai cũng có thể xem (nếu đã đăng nhập)
    @GetMapping
    public List<ClassesDTO> getAll() {
        return classesRepository.findAll()
                .stream()
                .map(ClassesDTO::new)
                .collect(Collectors.toList());
    }

   
    @GetMapping("/{id}")
    public ClassesDTO getById(@PathVariable Long id) {
        Classes clazz = classesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id " + id));
        return new ClassesDTO(clazz);
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN')")
    public ClassesDTO create(@RequestBody ClassesDTO dto) {
        Schools school = schoolsRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found with id " + dto.getSchoolId()));

        Classes clazz = new Classes();
        clazz.setName(dto.getName());
        clazz.setSchool(school);

        Classes saved = classesRepository.save(clazz);
        return new ClassesDTO(saved);
    }

    // Sửa lớp theo id: Chỉ SYSTEM hoặc SCHOOL_ADMIN được phép
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN')")
    
    public ClassesDTO update(@PathVariable Long id, @RequestBody ClassesDTO dto) {
        Classes clazz = classesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id " + id));

        clazz.setName(dto.getName());

        if (dto.getSchoolId() != null) {
            Schools school = schoolsRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found with id " + dto.getSchoolId()));
            clazz.setSchool(school);
        }

        Classes updated = classesRepository.save(clazz);
        return new ClassesDTO(updated);
    }

    // Xóa lớp theo id: Chỉ SYSTEM hoặc SCHOOL_ADMIN được phép
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN')")
    public String delete(@PathVariable Long id) {
        Classes clazz = classesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id " + id));
        classesRepository.delete(clazz);
        return "Deleted class with id " + id;
    }
}