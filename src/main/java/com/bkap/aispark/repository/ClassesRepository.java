package com.bkap.aispark.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.Classes;


@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {
	 Optional<Classes> findByName(String name);
}

