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

import com.bkap.aispark.entity.Student;
import com.bkap.aispark.service.StudentService;



@RestController
@RequestMapping("/api/students")
public class StudentApi {

    @Autowired private StudentService studentService;
    
    @GetMapping 
    public List<Student> getAllStudent(){
    	return studentService.getAllStudent();
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        Student saved = studentService.addStudent(student);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Student> getByIdStudent(@PathVariable Long id){
    	return studentService.getByIdStudent(id)
    	       .map(ResponseEntity::ok)
    	       .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id , @RequestBody Student student ){
    	try {
			 return studentService.updateStudent(id, student)
					 .map(ResponseEntity::ok)
					 .orElse(ResponseEntity.notFound().build());
		 }catch (RuntimeException e) {
			 return ResponseEntity.badRequest().body(null);
	        }
    	
    }
    
	 @DeleteMapping("/{id}")
	 public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
	     boolean deleted = studentService.deleteStudent(id);
	     return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
	 }
}