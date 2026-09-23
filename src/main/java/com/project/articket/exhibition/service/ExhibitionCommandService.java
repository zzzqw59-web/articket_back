package com.project.articket.exhibition.service;

import com.project.articket.common.util.CustomFileUtil;
import com.project.articket.exhibition.dto.ExhibitionDetailResponseDTO;
import com.project.articket.exhibition.dto.ExhibitionUpdateRequest;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.exhibition.util.ExhibitionPriceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class ExhibitionCommandService {

    private final ExhibitionRepository exhibitionRepository;
    private final CustomFileUtil customFileUtil;

    public ExhibitionDetailResponseDTO update(
            Long exhibitionId, ExhibitionUpdateRequest request, MultipartFile image) {

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new NoSuchElementException("전시를 찾을 수 없습니다."));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalStateException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        boolean free = ExhibitionPriceUtil.isFree(request.getPrice());

        exhibition.setExhibitionTitle(request.getTitle());
        exhibition.setStartDate(request.getStartDate());
        exhibition.setEndDate(request.getEndDate());
        exhibition.setExhibitionUrl(request.getUrl());
        exhibition.setExhibitionArea(request.getArea());
        exhibition.setExhibitionPrice(request.getPrice());
        exhibition.setIsFree(free);

        exhibition.setExhibitionTicketPrice( free ? null : (
                request.getTicketPrice() != null ? request.getTicketPrice() : ExhibitionPriceUtil.resolveTicketPrice(false)
                ));
        exhibition.setExhibitionDescription(request.getDescription());

        //이미지 새로 올라온 경우
        if (image != null && !image.isEmpty()) {
            String oldImage = exhibition.getExhibitionImgUrl();

            String newImage = customFileUtil.saveFile(image);

            exhibition.setExhibitionImgUrl(newImage);

            if(oldImage != null && !oldImage.isBlank()) {
                customFileUtil.deleteFile(oldImage);
            }
        }
        return toDetailDto(exhibition);
    }

    public void delate(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new NoSuchElementException("전시를 찾을 수 없습니다."));

        String imageFileName = exhibition.getExhibitionImgUrl();

        exhibitionRepository.de
    }
}
