package com.project.articket.venue.openapi.service;

import com.project.articket.common.util.GridConverter;
import com.project.articket.venue.openapi.dto.WeatherApiResponse;
import com.project.articket.venue.openapi.dto.WeatherDTO;
import com.project.articket.venue.entity.Venue;
import com.project.articket.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;
    private final VenueRepository venueRepository;

    @Value("${openapi.service-key}")
    private String serviceKey;

    public WeatherDTO getCurrentWeatherByVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NoSuchElementException("전시장을 찿을 수가 없습니다."));
        if (venue.getVenueLatitude() == null || venue.getVenueLongitude() == null) {
            throw new IllegalStateException("좌표 정보가 없어 날씨를 조회할 수 없습니다.");
        }

        GridConverter.Grid grid = GridConverter.toGrid(venue.getVenueLatitude(), venue.getVenueLongitude());
        BaseDateTime baseTime = resolveBaseDateTime(LocalDateTime.now());

        URI uri = buildWeatherUri(grid, baseTime);

        WeatherApiResponse response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(WeatherApiResponse.class)
                .block();
        return toDTO(response);
    }

    private URI buildWeatherUri(GridConverter.Grid grid, BaseDateTime baseTime) {
        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        return UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseTime.date())
                .queryParam("base_time", baseTime.time())
                .queryParam("nx", grid.nx())
                .queryParam("ny", grid.ny())
                .queryParam("numOfRows", 10)
                .queryParam("pageNo", 1)
                .build(true)
                .toUri();
    }

    private BaseDateTime resolveBaseDateTime(LocalDateTime now) {
        LocalDateTime adjusted = now.minusMinutes(40);
        return new BaseDateTime(
                adjusted.format(DateTimeFormatter.BASIC_ISO_DATE),
                adjusted.format(DateTimeFormatter.ofPattern("HH00"))
        );
    }

    private WeatherDTO toDTO(WeatherApiResponse response) {
        if (response == null || response.getResponse() == null
                || response.getResponse().getBody() == null
                || response.getResponse().getBody().getItems() == null) {
            throw new IllegalStateException("기상청 응답이 비어 있습니다.");
        }
        var heather = response.getResponse().getHeader();
        if(heather != null && !"00".equals(heather.getResultCode())) {
            throw new IllegalStateException("기상청 API 오류: " + heather.getResultCode() + " " + heather.getResultMsg());
        }
        Map<String, String> values = response.getResponse().getBody().getItems().getItem().stream()
                .collect(Collectors.toMap(WeatherApiResponse.Item::getCategory, WeatherApiResponse.Item::getObsrValue));

        return WeatherDTO.builder()
                .temperature(values.get("T1H"))
                .precipitationType(mapPty(values.get("PTY")))
                .humidity(values.get("REH"))
                .windDirection(mapWindDirection(values.get("VEC")))
                .windSpeed(values.get("WSD"))
                .build();
    }

    private String mapPty(String code) {
        return switch (code) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            default -> "알 수 없음";
        };
    }

    private String mapWindDirection(String degreeStr) {
        if (degreeStr == null) return "알 수 없음";
        try {
            double degree = Double.parseDouble(degreeStr);
            String[] directions = {"북", "북동", "동", "남동", "남", "남서", "서", "북서"};
            int index = (int) Math.round(degree / 45.0) % 8;
            return directions[index];
        } catch (NumberFormatException e) {
            return "알 수 없음";
        }
    }

    private record BaseDateTime(String date, String time) {}
}