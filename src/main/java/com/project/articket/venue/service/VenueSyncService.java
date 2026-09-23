package com.project.articket.venue.service;


import ch.qos.logback.classic.html.UrlCssBuilder;
import com.project.articket.venue.dto.*;
import com.project.articket.venue.entity.Venue;
import com.project.articket.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueSyncService {

    private final VenueRepository venueRepository;
    private final WebClient webClient;

    @Value("${openapi.service-key}")
    private String serviceKey;

    public void syncVenues() {
        List<VenueApiListItem> basicItems = new ArrayList<>();
        for(VenueCatagoryEndpoint category : VenueCatagoryEndpoint.values()) {
            basicItems.addAll(fetchAllPages(category));
        }
        for(VenueApiListItem basic : basicItems) {
            VenueApiDetailItem detail = fetchDetail(basic.getSeq());
            upsert(basic, detail);
        }
    }

    private List<VenueApiListItem> fetchAllPages(VenueCatagoryEndpoint category) {
        List<VenueApiListItem> result = new ArrayList<>();
        int page = 1;
        int totalCount = Integer.MAX_VALUE;

        while ((page - 1) * 100 < totalCount) {
            final int currentPage = page;
            VenueApiListResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https").host("apis.data.go.kr")
                            .path("B553457/nopenapi/rest/cultureartspaces" + category.path)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("numOfrows", 100)
                            .queryParam("pageNo", currentPage)
                            .build())
                    .retrieve()
                    .bodyToMono(VenueApiListResponse.class)
                    .block();
            totalCount = response.getBody().getTotalCount();
            result.addAll(response.getBody().getItems().getItem());
            page++;
        }
        return result;
    }

    private VenueApiDetailItem fetchDetail(Long seq) {
        VenueDetailResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https").host("apis.data.go,kr")
                        .path("B553457/nopenapi/rest/cultureartspaces/detail")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("seq", seq)
                        .build())
                .retrieve()
                .bodyToMono(VenueDetailResponse.class)
                .block();

        return response.getBody().getItems().getItem();
    }

    private void upsert(VenueApiListItem basic, VenueApiDetailItem detail) {
        Venue venue = venueRepository.findByVenueSeq(basic.getSeq())
                .orElseGet(Venue::new);

        venue.setVenueSeq(basic.getSeq());
        venue.setVenueTitle(basic.getCulName());
        venue.setVenueTel(basic.getCulTel());
        venue.setVenueUrl(basic.getCulHomeUrl());
        venue.setVenueLatitude(parseOrNull(basic.getGpsY()));
        venue.setVenueLongitude(parseOrNull(basic.getGpsX()));

        if(detail != null) {
            venue.setVenueDescription(Jsoup.parse(nullToEmpty(detail.getCulCont())).text());
            venue.setVenueImgUrl(detail.getCulViewImg1());
        }
        venueRepository.save(venue);
    }
    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
    private Double parseOrNull(String s) {
        try {
            return Double.parseDouble(s);
        }catch (Exception e) {
            return null;
        }
    }
}
