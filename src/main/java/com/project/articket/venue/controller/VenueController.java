package com.project.articket.venue.controller;

import com.project.articket.venue.dto.VenueDetailResponseDTO;
import com.project.articket.venue.dto.VenueSummaryDTO;
import com.project.articket.venue.dto.WeatherDTO;
import com.project.articket.venue.openapi.service.WeatherService;
import com.project.articket.venue.service.VenueQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueQueryService venueQueryService;
    private final WeatherService weatherService;

    @GetMapping
    public Page<VenueSummaryDTO> getVenues(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return venueQueryService.getVenueList(keyword, sort, page, size);
    }

    @GetMapping("/{venueId}")
    public VenueDetailResponseDTO getVenueDetail(@PathVariable Long venueId) {
        return venueQueryService.getVenueDetail(venueId);
    }

    @GetMapping("/{venueId}/weather")
    public WeatherDTO getVenueWeather(@PathVariable Long venueId) {
        return weatherService.getCurrentWeatherByVenue(venueId);
    }


}
