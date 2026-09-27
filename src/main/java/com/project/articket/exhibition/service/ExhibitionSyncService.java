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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionSyncService {

    private final ExhibitionRepository exhibitionRepository;
    private final VenueRepository venueRepository;
    private final WebClient webClient;

    @Value("${openapi.service-key}")
    private String serviceKey;

    public void syncExhibitions() {
        System.out.println("===== 전시 동기화 시작 =====");
        List<ExhibitionApiListItem> basicItems = fetchAllPages();
        System.out.println("===== 전시 목록 개수: " + basicItems.size() + " =====");

        for (ExhibitionApiListItem basic : basicItems) {
            System.out.println(
                    "제목 = " + basic.getTitle()
                            + " / realmName = " + basic.getRealmName()
                            + " / seq = " + basic.getSeq()
            );
            if (!"전시".equals(basic.getRealmName())) {
                System.out.println("→ 전시 아님, SKIP");
                continue;
            }
            if (exhibitionRepository.existsByExhibitionSeq(basic.getSeq())) {
                System.out.println("→ 이미 존재, SKIP");
                continue;
            }
            System.out.println("→ 저장 진행");

            ExhibitionApiDetailItem detail = fetchDetail(basic.getSeq());
            insert(basic, detail);

            //api호출시간간격 확보(이유는 많은 공공기반 api데이터를 딜레이없이 받아오면 429에러가뜨기때문)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private List<ExhibitionApiListItem> fetchAllPages() {
        List<ExhibitionApiListItem> result = new ArrayList<>();
        int page = 1;
        int totalCount = Integer.MAX_VALUE;

        while ((page - 1) * 100 < totalCount) {
            ExhibitionApiListResponse response = webClient.get()
                    .uri(buildListUri(page))
                    .retrieve()
                    .bodyToMono(ExhibitionApiListResponse.class)
                    .block();

            totalCount = response.getBody().getTotalCount();
            // 현재 페이지 데이터
            List<ExhibitionApiListItem> items =
                    response.getBody().getItems().getItem();

            System.out.println(
                    "페이지 = " + page
                            + " / 전체 데이터 = " + totalCount
                            + " / 현재 페이지 데이터 = " + items.size()
                            + " / 첫 seq = " + items.get(0).getSeq()
                            + " / 마지막 seq = " + items.get(items.size() - 1).getSeq()
            );

            result.addAll(response.getBody().getItems().getItem());
            page++;
        }
        System.out.println("최종 가져온 데이터 = " + result.size());
        return result;
    }

    private URI buildListUri(int page) {
        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        return UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/B553457/cultureinfo/realm2")
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("realmCode", "D000")
                .queryParam("serviceTp", "A")
                .queryParam("numOfrows", 100)
                .queryParam("PageNo", page)
                .build(true)   // 이미 인코딩된 값이니 재인코딩 금지
                .toUri();
    }

    private ExhibitionApiDetailItem fetchDetail(Long seq) {
        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/B553457/cultureinfo/detail2")
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("seq", seq)
                .build(true)
                .toUri();

        ExhibitionApiDetailResponse response = webClient.get()
                .uri(uri)
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

        if (detail != null) {
            exhibition.setExhibitionUrl(detail.getUrl());
            exhibition.setExhibitionPrice(detail.getPrice());
            exhibition.setExhibitionImgUrl(detail.getImgUrl());
            exhibition.setExhibitionDescription(detail.getContents1());

            boolean free = ExhibitionPriceUtil.isFree(detail.getPrice());
            exhibition.setIsFree(free);
            exhibition.setExhibitionTicketPrice(ExhibitionPriceUtil.resolveTicketPrice(free));

            Long placeSeq = parseLongOrNull(detail.getPlaceSeq());
            System.out.println(
                    "전시명 = " + basic.getTitle()
                            + " / placeSeq = " + detail.getPlaceSeq()
                            + " / 변환값 = " + placeSeq
            );
            if (placeSeq != null) {
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
        } catch (Exception e) {
            return null;
        }
    }
}