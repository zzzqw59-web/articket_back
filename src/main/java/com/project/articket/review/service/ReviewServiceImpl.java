package com.project.articket.review.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.review.dto.ReviewCreateDTO;
import com.project.articket.review.dto.ReviewDTO;
import com.project.articket.review.dto.ReviewUpdateDTO;
import com.project.articket.review.entity.Review;
import com.project.articket.review.entity.ReviewImage;
import com.project.articket.review.repository.ReviewImageRepository;
import com.project.articket.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository repository;
    private final MemberRepository memberRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Value("${com.spring.website.upload.path}")
    private String fileDir;

    @Override
    public PageResponseDTO<ReviewDTO> reviewPage(PageRequestDTO pageRequestDTO) {
        Page<Review> page = repository.findAll(pageRequestDTO.getPageable("reviewCreatedAt"));

        List<ReviewDTO> dtoList = page.getContent().stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setReviewId(review.getReviewId());
            dto.setMemberName(review.getMember().getMemberName());
            dto.setExhibitionTitle(review.getExhibition().getExhibitionTitle());
            dto.setReviewTitle(review.getReviewTitle());
            dto.setReviewBody(review.getReviewBody());
            dto.setReviewCreatedAt(review.getReviewCreatedAt());
            dto.setReviewModifiedAt(review.getReviewModifiedAt());
            dto.setReviewHits(review.getReviewHits());
            return dto;
        }).toList();
        return new PageResponseDTO<>(dtoList, pageRequestDTO, page.getTotalElements());
    }


    @Override
    public void reviewCreate(Long memberId, ReviewCreateDTO reviewCreateDTO) {
        List<MultipartFile> images = reviewCreateDTO.getImages();

        // 이미지 갯수 검증
        if (images != null && images.size() > 3) {
            throw new IllegalArgumentException("리뷰 이미지는 최대 3개까지 등록 가능합니다.");
        }

        // 이미지 파일 검증
        if (images != null) {
            for (MultipartFile image : images) {
                if (image.getSize() > 5 * 1024 * 1024) {
                    throw new IllegalArgumentException("이미지는 5MB 이하만 등록 가능합니다.");
                }

                String contentType = image.getContentType();

                if (!List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
                    throw new IllegalArgumentException("JPG, JPEG, PNG, WEBP 이미지만 등록 가능합니다.");
                }
            }
        }

        Exhibition exhibition = exhibitionRepository.findById(reviewCreateDTO.getExhibitionId()).orElseThrow(() -> new IllegalArgumentException("전시를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        Review review = new Review(member, exhibition, reviewCreateDTO.getReviewTitle(), reviewCreateDTO.getReviewBody());
        repository.save(review);

        // 이미지가 있을 경우에만 파일 저장
        if (images != null && !images.isEmpty()) {
            Path uploadPath = Paths.get(fileDir, "review");
            try {
                Files.createDirectories(uploadPath);
                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);

                    String originalFilename = image.getOriginalFilename();
                    String uuid = UUID.randomUUID().toString();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String savedFilename = uuid + extension;

                    Path savePath = uploadPath.resolve(savedFilename);

                    image.transferTo(savePath);

                    String imageUrl = "/upload/review/" + savedFilename;

                    ReviewImage reviewImage = new ReviewImage(review, originalFilename, savedFilename, imageUrl, i + 1);

                    reviewImageRepository.save(reviewImage);
                }
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 경로 생성에 실패하였습니다.", e);
            }
        }
    }
    @Transactional
    @Override
    public void reviewUpdate(Long reviewId, ReviewUpdateDTO reviewUpdateDTO) {
        Review review = repository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
        review.setReviewTitle(reviewUpdateDTO.getReviewTitle());
        review.setReviewBody(reviewUpdateDTO.getReviewBody());
    }
    @Transactional
    @Override
    public void reviewDelete(Long reviewId) {
        Review review = repository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
        repository.delete(review);
    }

    @Transactional
    @Override
    public ReviewDTO reviewDetail(Long reviewId) {
        Review review = repository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        review.setReviewHits(review.getReviewHits() + 1);

        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(reviewId);
        dto.setMemberName(review.getMember().getMemberName());
        dto.setReviewTitle(review.getReviewTitle());
        dto.setReviewBody(review.getReviewBody());
        dto.setReviewCreatedAt(review.getReviewCreatedAt());
        dto.setReviewModifiedAt(review.getReviewModifiedAt());
        dto.setExhibitionTitle(review.getExhibition().getExhibitionTitle());
        dto.setReviewHits(review.getReviewHits());

        return dto;
    }
}
