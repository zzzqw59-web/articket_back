package com.project.articket.review.service;

import org.springframework.web.multipart.MultipartFile;

public interface ReviewImageService {
    void reviewImageCreate(MultipartFile multipartFile);
}
