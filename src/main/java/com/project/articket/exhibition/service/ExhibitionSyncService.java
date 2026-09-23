package com.project.articket.exhibition.service;

import com.project.articket.exhibition.dto.ExhibitionApiDetailItem;
import com.project.articket.exhibition.dto.ExhibitionApiDetailResponse;
import com.project.articket.exhibition.dto.ExhibitionApiListItem;
import com.project.articket.exhibition.dto.ExhibitionApiListResponse;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.exhibition.util.ExhibitionPriceUtil;
import com.project.articket.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

@Service
@RequiredArgsConstructor
public class ExhibitionSyncService {

    private final ExhibitionRepository exhibitionRepository;
    private final VenueRepository venueRepository;
    private final WebClient webClient;

    @Value("${openapi.service-key}")
    private String serviceKey;

    public void syncExhibitions() {
        List<ExhibitionApiListItem> basicItems = fetchAllPages();

        for(ExhibitionApiListItem basic : basicItems) {
            if(!"전시".equals(basic.getRealmName())) {
                continue;
            }
            if (exhibitionRepository.existsByExhibitionSeq(basic.getSeq())) {
                continue;
            }

            ExhibitionApiDetailItem detail = fetchDetail(basic.getSeq());
            insert(basic, detail);
        }
    }

    private List<ExhibitionApiListItem> fetchAllPages() {
        List<ExhibitionApiListItem> result = new ArrayList<>();
        int page = 1;
        int totalCount = Integer.MAX_VALUE;

        while ((page - 1) * 100 < totalCount) {
            final int currentPage = page;

            ExhibitionApiListResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https").host("apis.data.go.kr")
                            .path("B553457/cultureinfo/realm2")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("realmCode", "D000")
                            .queryParam("serviceTp", "A")
                            .queryParam("numofrows", 100)
                            .queryParam("pageNo", currentPage)
                            .build())
                    .retrieve()
                    .bodyToMono(ExhibitionApiListResponse.class)
                    .block();
            totalCount = response.getBody().getTotalCount();
            result.addAll(response.getBody().getItems().getItem());
            page++;
        }
        return result;
    }

    private ExhibitionApiDetailItem fetchDetail(Long seq) {
        ExhibitionApiDetailResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https").host("apis.data.go.kr")
                        .path("/B553457/cultureinfo/detail2")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("seq", seq)
                        .build())
                .retrieve()
                .bodyToMono(ExhibitionApiDetailResponse.class)
                .block();
        return response.getBody().getItems().getItem();
    }

    private void insert(ExhibitionApiListItem basic, ExhibitionApiDetailItem detail) {
        Exhibition exhibition = new Exhibition();

        exhibition.setExhibitionSeq(basic.getSeq());
        exhibition.setExhibitionTitle(basic.getTitle());
        exhibition.setStartDate(parseDate(basic.getStartDate()));
        exhibition.setEndDate(parseDate(basic.getEndDate()));
        exhibition.setExhibitionArea(basic.getArea());

        if(detail != null) {
            exhibition.setExhibitionUrl(detail.getUrl());
            exhibition.setExhibitionPrice(detail.getPrice());
            exhibition.setExhibitionImgUrl(detail.getImgUrl());
            exhibition.setExhibitionDescription(detail.getContents1());

            boolean free = ExhibitionPriceUtil.isFree(detail.getPrice());
            exhibition.setIsFree(free);
            exhibition.setExhibitionTicketPrice(ExhibitionPriceUtil.resolveTicketPrice(free));

            Long placeSeq = parseLongOrNull(detail.getPlaceSeq());
            if(placeSeq != null) {
                venueRepository.findByVenueSeq(placeSeq)
                        .ifPresent(exhibition::setVenue);
            }
        } else {
            exhibition.setExhibitionImgUrl(basic.getThumbnail());
        }

        exhibitionRepository.save(exhibition);
    }
    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }
    private Long parseLongOrNull(String s) {
        try {
            return Long.parseLong(s);
        }catch (Exception e) {
            return null;
        }
    }
}
