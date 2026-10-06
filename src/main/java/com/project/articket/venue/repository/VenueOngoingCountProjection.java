package com.project.articket.venue.repository;

import java.time.LocalDate;

public interface VenueOngoingCountProjection {
    Long getId();
    String getName();
    String getPhotoUrl();
    String getTel();
    Long getOngoingCount();
    LocalDate getLatestExhibitionDate();
}
