package com.project.articket.exhibition.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.jsonwebtoken.Header;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JacksonXmlRootElement(localName = "response")
@Setter
@Getter
public class ExhibitionApiListResponse {
    private Header header;
    private Body body;

    @Getter @Setter
    public static class Body {
        private Items items;
        private int totalCount;
    }

    @Getter @Setter
    public static class Items {
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<ExhibitionApiListItem> item;
    }
}
