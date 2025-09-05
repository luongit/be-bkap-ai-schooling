package com.bkap.aispark.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/class")
public class ClassApi {

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private SchoolsRepository schoolsRepository;

    @GetMapping
    public List<ClassesDTO> getAll() {
        return classesRepository.findAll()
                .stream()
                .map(ClassesDTO::new)
                .collect(Collectors.toList());
    }

    // Lấy lớp theo id
    @GetMapping("/{id}")
    public ClassesDTO getById(@PathVariable Long id) {
        Classes clazz = classesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id " + id));
        return new ClassesDTO(clazz);
    }

    // Thêm lớp mới
    @PostMapping
    public ClassesDTO create(@RequestBody ClassesDTO dto) {
        Schools school = schoolsRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found with id " + dto.getSchoolId()));

        Classes clazz = new Classes();
        clazz.setName(dto.getName());
        clazz.setSchool(school);

        Classes saved = classesRepository.save(clazz);
        return new ClassesDTO(saved);
    }

    // Sửa lớp theo id
    @PutMapping("/{id}")
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

    // Xóa lớp theo id
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        Classes clazz = classesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id " + id));
        classesRepository.delete(clazz);
        return "Deleted class with id " + id;
    }
}
