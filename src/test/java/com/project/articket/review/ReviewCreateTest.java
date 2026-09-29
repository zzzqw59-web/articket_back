package com.project.articket.review;

import com.project.articket.review.dto.ReviewCreateDTO;
import com.project.articket.review.dto.ReviewUpdateDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ReviewCreateTest {

    private static Validator validator;
    private static ValidatorFactory factory;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void reviewCreateTest() {
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setExhibitionId(1L);
        dto.setReviewTitle("좋은 전시입니다.");
        dto.setReviewBody("굿굿굿");

        Set violation = validator.validate(dto);
        assertThat(violation).isEmpty();
    }

    @Test
    void reviewUpdateTest() {
        ReviewUpdateDTO dto = new ReviewUpdateDTO();
        dto.setReviewTitle("변경되는 제목");
        dto.setReviewBody("변경되는 내용");

        Set violation = validator.validate(dto);
        assertThat(violation).isEmpty();
    }

    @Test
    void reviewNotCreateTest() {
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setExhibitionId(1L);
        dto.setReviewTitle("테스트");

        Set violation = validator.validate(dto);
        assertThat(violation).isNotEmpty();
    }
}
