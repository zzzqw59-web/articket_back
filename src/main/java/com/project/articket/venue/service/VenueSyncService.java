package com.project.articket.venue.service;

import com.project.articket.venue.dto.*;
import com.project.articket.venue.entity.Venue;
import com.project.articket.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        for (VenueCategoryEndpoint category : VenueCategoryEndpoint.values()) {
            basicItems.addAll(fetchAllPages(category));
        }
        for (VenueApiListItem basic : basicItems) {
            VenueApiDetailItem detail = fetchDetail(basic.getSeq());
            upsert(basic, detail);

            // API 호출 간격 조절
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("API 호출 대기 중 인터럽트 발생", e);
            }
        }
    }

    private List<VenueApiListItem> fetchAllPages(VenueCategoryEndpoint category) {
        List<VenueApiListItem> result = new ArrayList<>();
        int page = 1;
        int totalCount = Integer.MAX_VALUE;

        while ((page - 1) * 100 < totalCount) {
            VenueApiListResponse response = webClient.get()
                    .uri(buildListUri(category, page))
                    .retrieve()
                    .bodyToMono(VenueApiListResponse.class)
                    .block();

            totalCount = response.getBody().getTotalCount();

            System.out.println(
                    category + " / page=" + page +
                            " / totalCount=" + totalCount +
                            " / 현재 조회=" + result.size()
            );
            result.addAll(response.getBody().getItems().getItem());
            page++;

            // API 호출 간격 조절
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("API 호출 대기 중 인터럽트 발생", e);
            }
        }
        return result;
    }

    private URI buildListUri(VenueCategoryEndpoint category, int page) {
        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        return UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/B553457/nopenapi/rest/cultureartspaces" + category.path)
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("numOfrows", 100)
                .queryParam("pageNo", page)
                .build(true)
                .toUri();
    }

    private VenueApiDetailItem fetchDetail(Long seq) {
        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/B553457/nopenapi/rest/cultureartspaces/detail")
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("seq", seq)
                .build(true)
                .toUri();

        VenueDetailResponse response = webClient.get()
                .uri(uri)
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

        if (detail != null) {
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
        } catch (Exception e) {
            return null;
        }
    }
}