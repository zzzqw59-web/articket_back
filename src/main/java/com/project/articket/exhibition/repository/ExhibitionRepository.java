package com.project.articket.exhibition.repository;

import com.project.articket.exhibition.entity.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
}
