package com.project.articket.exhibition.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExhibitionApiDetailItem {
    private Long seq;
    private String price;
    private String contents1;
    private String url;
    private String imgUrl;
    private String placeSeq;
}
