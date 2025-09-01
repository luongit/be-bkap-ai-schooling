package com.bkap.aispark.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonInclude;


@Entity
@Table(name = "import_logs")
public class ImportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "imported_by")
    private Long importedBy;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String errors;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


	public ImportLog() {
		super();
		// TODO Auto-generated constructor stub
	}


	public ImportLog(Long id, String fileName, Long importedBy, Integer totalRecords, Integer successCount,
			Integer errorCount, String errors, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.importedBy = importedBy;
		this.totalRecords = totalRecords;
		this.successCount = successCount;
		this.errorCount = errorCount;
		this.errors = errors;
		this.createdAt = createdAt;
	}


	public  Long getId() {
		return id;
	}


	public  void setId(Long id) {
		this.id = id;
	}


	public  String getFileName() {
		return fileName;
	}


	public  void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public  Long getImportedBy() {
		return importedBy;
	}


	public  void setImportedBy(Long importedBy) {
		this.importedBy = importedBy;
	}


	public  Integer getTotalRecords() {
		return totalRecords;
	}


	public  void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}


	public  Integer getSuccessCount() {
		return successCount;
	}


	public  void setSuccessCount(Integer successCount) {
		this.successCount = successCount;
	}


	public  Integer getErrorCount() {
		return errorCount;
	}


	public  void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}


	public  String getErrors() {
		return errors;
	}


	public  void setErrors(String errors) {
		this.errors = errors;
	}


	public  LocalDateTime getCreatedAt() {
		return createdAt;
	}


	public  void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

   
    
}
