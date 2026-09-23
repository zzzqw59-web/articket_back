package com.project.articket.venue.service;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.venue.dto.VenueDetailResponseDTO;
import com.project.articket.venue.dto.VenueExhibitionItemDTO;
import com.project.articket.venue.dto.VenueSummaryDTO;
import com.project.articket.venue.entity.Venue;
import com.project.articket.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VenueQueryService {

    private final VenueRepository venueRepository;
    private final ExhibitionRepository exhibitionRepository;

    public Page<VenueSummaryDTO> getVenueList(String keyword, String sortOption, int page, int size) {
        Sort sort = "oldest".equals(sortOption)
                ? Sort.by("latestExhibitionDate").ascending()
                : Sort.by("latestExhibitionDate").descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return venueRepository.findVenueListWithOngoingCount(LocalDate.now(), keyword, pageable)
                .map(p -> VenueSummaryDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .photoUrl(p.getPhotoUrl())
                        .tel(p.getTel())
                        .ongoingCount(p.getOngoingCount())
                        .build());
    }

    public VenueDetailResponseDTO getVenueDetail(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NoSuchElementException("전시장을 찿을 수 없습니다."));

        LocalDate today = LocalDate.now();

        List<VenueExhibitionItemDTO> exhibitions = exhibitionRepository.findByVenueVenueId(venueId).stream()
                .filter(e -> e.getEndDate() != null && !e.getEndDate().isBefore(today))
                .sorted(Comparator.comparing(Exhibition::getStartDate))
                .map(e -> VenueExhibitionItemDTO.builder()
                        .id(e.getExhibitionId())
                        .title(e.getExhibitionTitle())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .imgUrl(e.getExhibitionImgUrl())
                        .status(today.isBefore(e.getStartDate()) ? "예정" : "진행중")
                        .build())
                .toList();

        return VenueDetailResponseDTO.builder()
                .id(venue.getVenueId())
                .name(venue.getVenueTitle())
                .description(venue.getVenueDescription())
                .photoUrl(venue.getVenueImgUrl())
                .latitude(venue.getVenueLatitude())
                .longitude(venue.getVenueLongitude())
                .tel(venue.getVenueTel())
                .honePageUrl(venue.getVenueUrl())
                .exhibitions(exhibitions)
                .build();
    }
}
