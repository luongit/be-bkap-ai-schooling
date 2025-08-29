package com.bkap.aispark.api;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.Teacher;
import com.bkap.aispark.service.TeacherService;



@RestController
@RequestMapping("/api/teachers")
public class TeacherApi {

    @Autowired private TeacherService teacherService;

    @GetMapping
    public List<Teacher> getAllTeacher(){
    	return teacherService.getAllTeacher();
    }
    
    @PostMapping
    public ResponseEntity<Teacher> addTeacher(@RequestBody Teacher teacher) {
        Teacher saved = teacherService.addTeacher(teacher);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getByIdTeacher(@PathVariable Long id){
    	return teacherService.getByIdTeacher(id)
    			.map(ResponseEntity::ok)
    			.orElse(ResponseEntity.notFound().build());			
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id , @RequestBody Teacher teacher){
    	try {
			return teacherService.updateTeacher(id, teacher)
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		} catch (RuntimeException e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().body(null);
		}
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
    	boolean deleted = teacherService.deleteTeacher(id);
    	return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    
}


