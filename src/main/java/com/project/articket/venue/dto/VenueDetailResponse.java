package com.project.articket.venue.dto;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import com.project.articket.common.dto.ApiHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "response")
public class VenueDetailResponse {
    private ApiHeader header;
    private Body body;

    @Getter
    @Setter
    public static class Body {
        private Items items;
    }

    @Getter
    @Setter
    public static class Items {
        private VenueApiDetailItem item;
    }
}
