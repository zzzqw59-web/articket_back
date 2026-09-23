package com.project.articket.exhibition.dto;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.jsonwebtoken.Header;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "response")
public class ExhibitionApiDetailResponse {
    private Header header;
    private Body Body;

    @Getter @Setter
    public static class Body {
        private Items items;
    }
    @Getter @Setter
    public static class Items {
        private ExhibitionApiDetailItem item;
    }
}
