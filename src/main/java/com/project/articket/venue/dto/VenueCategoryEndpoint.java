package com.project.articket.venue.dto;

public enum VenueCategoryEndpoint {
    MUSEUM("/museum"),
    HALL("/hall"),
    LIBRARY("/library"),
    PERFORMING_PLACE("/performingplace"),
    ART_GALLERY("/artgallery");

    public final String path;
    VenueCategoryEndpoint(String path) {
        this.path = path;
    }
}
