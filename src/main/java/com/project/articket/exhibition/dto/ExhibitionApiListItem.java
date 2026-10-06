package com.project.articket.exhibition.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExhibitionApiListItem {
    private Long seq;
    private String title;
    private String startDate;
    private String endDate;
    private String realmName;
    private String area;
    private String thumbnail;
}
