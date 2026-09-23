package com.project.articket.venue.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class WeatherDTO {
    private String temperature;
    private String precipitationType;
    private String humidity;
    private String windDirection;
    private String windSpeed;
}
