package com.project.articket.exhibition.service;

import com.project.articket.exhibition.dto.ExhibitionDetailResponseDTO;
import com.project.articket.exhibition.dto.ExhibitionListItemDTO;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.venue.dto.VenueSummaryDTO;
import com.project.articket.venue.entity.Venue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ExhibitionQueryService {

    private final ExhibitionRepository exhibitionRepository;

    public Page<ExhibitionListItemDTO> searchExhibitions(
            String keyword, String sortOption, boolean isFree, int page, int size) {

        Sort sort = "oldest".equals(sortOption)
                ? Sort.by("startDate").ascending()
                : Sort.by("startDate").descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return exhibitionRepository.seqrchByFree(isFree, keyword, pageable)
                .map(e -> ExhibitionListItemDTO.builder()
                        .id(e.getExhibitionId())
                        .title(e.getExhibitionTitle())
                        .imgUrl(e.getExhibitionImgUrl())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .venueName(e.getVenue() != null ? e.getVenue().getVenueTitle() : null)
                        .free(e.getIsFree())
                        .build());
    }

    public ExhibitionDetailResponseDTO getExhibitionDetail(Long exhibitionId) {
        Exhibition e = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new NoSuchElementException("전시를 찾을 수 없습니다."));

        VenueSummaryDTO venueDto = null;
        if(e.getVenue() != null) {
            Venue v = e.getVenue();
            venueDto = VenueSummaryDTO.builder()
                    .id(v.getVenueId())
                    .name(v.getVenueTitle())
                    .photoUrl(v.getVenueImgUrl())
                    .tel(v.getVenueTel())
                    .build();
        }

        return ExhibitionDetailResponseDTO.builder()
                .id(e.getExhibitionId())
                .title(e.getExhibitionTitle())
                .description(e.getExhibitionDescription())
                .url(e.getExhibitionUrl())
                .imgUrl(e.getExhibitionImgUrl())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .area(e.getExhibitionArea())
                .price(e.getExhibitionPrice())
                .ticketPrice(e.getExhibitionTicketPrice())
                .free(e.getIsFree())
                .venue(venueDto)
                .build();
    }
}
