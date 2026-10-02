package com.project.articket.venue.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VenueApiListItem {
    private Long seq;
    private String culName;
    private String culTel;
    private String culHomeUrl;
    private String gpsX;
    private String gpsY;
}
