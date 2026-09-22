package com.project.articket.ask.repository;

import com.project.articket.ask.entity.Ask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AskRepository extends JpaRepository<Ask, Long>, AskRepositoryCustom {
}