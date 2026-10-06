package com.project.articket.review.service;

import com.project.articket.common.crypto.PersonalDataCrypto;
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
import com.project.articket.review.entity.ReviewReply;
import com.project.articket.review.repository.ReviewImageRepository;
import com.project.articket.review.repository.ReviewReplyRepository;
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
    private final ReviewReplyRepository reviewReplyRepository;
    private final PersonalDataCrypto personalDataCrypto;

    @Value("${com.spring.website.upload.path}")
    private String fileDir;

    @Override
    public PageResponseDTO<ReviewDTO> reviewPage(PageRequestDTO pageRequestDTO) {
        Page<Review> page = repository.findAll(pageRequestDTO.getPageable("reviewCreatedAt"));

        List<ReviewDTO> dtoList = page.getContent().stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setReviewId(review.getReviewId());
            dto.setMemberName(
                    personalDataCrypto.decryptName(
                            review.getMember().getMemberName()
                    )
            );
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
    public PageResponseDTO<ReviewDTO> reviewSearch(String searchType, String keyword, PageRequestDTO pageRequestDTO) {
        Page<Review> page;

        if ("title".equals(searchType)) {
            page = repository.findByReviewTitleContaining(keyword, pageRequestDTO.getPageable("reviewCreatedAt"));
        } else if ("all".equals(searchType)) {
            page = repository.findByReviewTitleContainingOrReviewBodyContainingOrMemberMemberNameContainingOrExhibitionExhibitionTitleContaining(keyword, keyword, keyword, keyword, pageRequestDTO.getPageable("reviewCreatedAt"));
        } else if ("exhibition".equals(searchType)) {
            page = repository.findByExhibitionExhibitionTitleContaining(keyword, pageRequestDTO.getPageable("reviewCreatedAt"));
        } else if ("writer".equals(searchType)) {
            page = repository.findByMemberMemberNameContaining(keyword, pageRequestDTO.getPageable("reviewCreatedAt"));
        } else {
            page = repository.findByReviewTitleContainingOrReviewBodyContaining(
                    keyword,
                    keyword,
                    pageRequestDTO.getPageable("reviewCreatedAt"));
        }

        List<ReviewDTO> dtoList = page.getContent().stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setReviewId(review.getReviewId());
            dto.setMemberName(
                    personalDataCrypto.decryptName(
                            review.getMember().getMemberName()
                    )
            );
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


        boolean exists = repository.existsByMemberMemberIdAndExhibitionExhibitionId(
                memberId,
                reviewCreateDTO.getExhibitionId()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 해당 전시에 대한 리뷰를 작성했습니다.");
        }

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

        Exhibition exhibition = exhibitionRepository.findById(reviewCreateDTO.getExhibitionId())
                .orElseThrow(() -> new IllegalArgumentException("전시를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        Review review = new Review(
                member,
                exhibition,
                reviewCreateDTO.getReviewTitle(),
                reviewCreateDTO.getReviewBody()
        );

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

                    ReviewImage reviewImage = new ReviewImage(
                            review,
                            originalFilename,
                            savedFilename,
                            imageUrl,
                            i + 1
                    );

                    reviewImageRepository.save(reviewImage);
                }

            } catch (IOException e) {
                throw new RuntimeException("파일 저장 경로 생성에 실패하였습니다.", e);
            }
        }
    }

    @Transactional
    @Override
    public void reviewUpdate(Long memberId, Long reviewId, ReviewUpdateDTO reviewUpdateDTO) {
        Review review = repository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        if (!review.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.setReviewTitle(reviewUpdateDTO.getReviewTitle());
        review.setReviewBody(reviewUpdateDTO.getReviewBody());

        List<MultipartFile> images = reviewUpdateDTO.getImages();

        List<ReviewImage> reviewImages =
                reviewImageRepository.findByReviewReviewIdOrderByReviewImageOrder(reviewId);

        int currentImageCount = reviewImages.size();
        int deleteImageCount =
                reviewUpdateDTO.getDeleteImageIds() == null
                        ? 0
                        : reviewUpdateDTO.getDeleteImageIds().size();

        int newImageCount =
                images == null
                        ? 0
                        : images.size();

        if (currentImageCount - deleteImageCount + newImageCount > 3) {
            throw new IllegalArgumentException("등록할 수 있는 이미지는 최대 3개입니다.");
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

        if (reviewUpdateDTO.getDeleteImageIds() != null) {
            for (Long deleteImageId : reviewUpdateDTO.getDeleteImageIds()) {
                ReviewImage reviewImage = reviewImageRepository.findById(deleteImageId)
                        .orElseThrow(() -> new IllegalArgumentException("이미지가 존재하지 않습니다."));

                if (!reviewImage.getReview().getReviewId().equals(reviewId)) {
                    throw new IllegalArgumentException("해당 리뷰의 이미지가 아닙니다.");
                }

                String reviewImageFilename =
                        reviewImage.getReviewImageFilename();

                Path uploadPath =
                        Paths.get(fileDir, "review");

                Path deletePath =
                        uploadPath.resolve(reviewImageFilename);

                try {
                    Files.delete(deletePath);
                } catch (IOException e) {
                    throw new RuntimeException(
                            "이미지 파일 삭제에 실패했습니다.",
                            e
                    );
                }

                reviewImageRepository.delete(reviewImage);
            }
        }

        List<ReviewImage> remainingImages =
                reviewImageRepository.findByReviewReviewIdOrderByReviewImageOrder(reviewId);

        for (int i = 0; i < remainingImages.size(); i++) {
            remainingImages.get(i).setReviewImageOrder(i + 1);
        }

        if (images != null && !images.isEmpty()) {
            Path uploadPath =
                    Paths.get(fileDir, "review");

            try {
                Files.createDirectories(uploadPath);

                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);

                    String originalFilename =
                            image.getOriginalFilename();

                    String uuid =
                            UUID.randomUUID().toString();

                    String extension =
                            originalFilename.substring(
                                    originalFilename.lastIndexOf(".")
                            );

                    String savedFilename =
                            uuid + extension;

                    Path savePath =
                            uploadPath.resolve(savedFilename);

                    image.transferTo(savePath);

                    String imageUrl =
                            "/upload/review/" + savedFilename;

                    int imageOrder =
                            remainingImages.size() + i + 1;

                    ReviewImage reviewImage =
                            new ReviewImage(
                                    review,
                                    originalFilename,
                                    savedFilename,
                                    imageUrl,
                                    imageOrder
                            );

                    reviewImageRepository.save(reviewImage);
                }

            } catch (IOException e) {
                throw new RuntimeException(
                        "파일 저장에 실패하였습니다.",
                        e
                );
            }
        }
    }

    @Transactional
    @Override
    public void reviewDelete(Long memberId, Long reviewId) {
        Review review = repository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        if (!review.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        List<ReviewImage> reviewImages =
                reviewImageRepository.findByReviewReviewIdOrderByReviewImageOrder(reviewId);

        List<ReviewReply> reviewReplies =
                reviewReplyRepository.findByReviewReviewId(reviewId);

        for (ReviewReply reviewReply : reviewReplies) {
            reviewReplyRepository.delete(reviewReply);
        }

        Path uploadPath =
                Paths.get(fileDir, "review");

        for (ReviewImage reviewImage : reviewImages) {
            Path deletePath =
                    uploadPath.resolve(
                            reviewImage.getReviewImageFilename()
                    );

            try {
                Files.delete(deletePath);
            } catch (IOException e) {
                throw new RuntimeException(
                        "이미지가 존재하지 않습니다.",
                        e
                );
            }

            reviewImageRepository.delete(reviewImage);
        }

        repository.delete(review);
    }

    @Transactional
    @Override
    public ReviewDTO reviewDetail(Long reviewId) {
        Review review = repository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        review.setReviewHits(
                review.getReviewHits() + 1
        );

        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(reviewId);
        dto.setMemberName(
                personalDataCrypto.decryptName(
                        review.getMember().getMemberName()
                )
        );
        dto.setReviewTitle(review.getReviewTitle());
        dto.setReviewBody(review.getReviewBody());
        dto.setReviewCreatedAt(review.getReviewCreatedAt());
        dto.setReviewModifiedAt(review.getReviewModifiedAt());
        dto.setExhibitionTitle(
                review.getExhibition().getExhibitionTitle()
        );
        dto.setReviewHits(review.getReviewHits());

        return dto;
    }
}