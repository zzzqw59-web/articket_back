package com.project.articket.venue.dto;

public enum VenueCatagoryEndpoint {
    MUSEUM("/museum"),
    HALL("/hall"),
    LIBRARY("/library"),
    PERFORMING_PLACE("/performingplace"),
    ART_GALLERY("/artgallery");

    public final String path;
    VenueCatagoryEndpoint(String path) {
        this.path = path;
    }
}
