package com.project.articket.venue.entity;

import com.project.articket.exhibition.entity.Exhibition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "VENUE")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venueId;

    @Column(name = "VENUE_SEQ", unique = true)
    private Long venueSeq;

    @Column(name = "VENUE_TITLE")
    private String venueTitle;

    @Column(name = "VENUE_TEL")
    private String venueTel;

    @Column(name = "VENUE_DESCRIPTION")
    @Lob
    private String venueDescription;

    @Column(name = "VENUE_IMG_URL")
    private  String venueImgUrl;

    @Column(name = "VENUE_URL")
    private String venueUrl;

    @Column(name = "VENUE_LONGITUDE")
    private Double venueLongitude;

    @Column(name = "VENUE_LATITUDE")
    private Double venueLatitude;

    @Column(name = "VENUE_CREATED_AT", nullable = false)
    @CreationTimestamp
    private LocalDateTime venueCreatedAt;

    @OneToMany(mappedBy = "venue")
    private List<Exhibition> exhibitionList = new ArrayList<>();
}