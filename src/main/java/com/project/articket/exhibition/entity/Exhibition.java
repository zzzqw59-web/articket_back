package com.project.articket.exhibition.entity;

import com.project.articket.venue.entity.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "EXHIBITION")
public class Exhibition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exhibitionId;

    @Column(name = "EXHIBITION_SEQ", unique = true)
    private Long exhibitionSeq;

    @Column(name = "EXHIBITION_TITLE")
    private String exhibitionTitle;

    @Column(name = "EXHIBITION_START_CREATED_AT")
    private LocalDate startDate;

    @Column(name = "EXHIBITION_END_CREATED_AT")
    private LocalDate endDate;

    @Column(name = "EXHIBITION_URL")
    private String exhibitionUrl;

    @Column(name = "EXHIBITION_AREA")
    private String exhibitionArea;

    @Column(name = "EXHIBITION_PRICE")
    private String exhibitionPrice;

    @Column(name = "EXHIBITION_TICKET_PRICE")
    private Integer exhibitionTicketPrice;

    @Column(name = "EXHIBITION_IMG_URL")
    private String exhibitionImgUrl;

    @Column(name = "EXHIBITION_DESCRIPTION")
    @Lob
    private String exhibitionDescription;

    @Column(name = "EXHIBITION_IS_FREE")
    private Boolean isFree;

    @Column(name = "EXHIBITION_CREATED_AT" , updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime exhibitionCreatedAt;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;
}

