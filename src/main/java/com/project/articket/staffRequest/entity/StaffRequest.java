package com.project.articket.staffRequest.entity;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import com.project.articket.staffRequest.enums.StaffRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "STAFF_REQUEST")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StaffRequest {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "STAFF_REQUEST_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "STAFF_REQUEST_SEQ_GEN",
            sequenceName = "STAFF_REQUEST_SEQ",
            allocationSize = 1
    )
    @Column(name = "STAFF_REQUEST_ID")
    private Long staffRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "MEMBER_ID",
            nullable = false
    )
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "EXHIBITION_ID",
            nullable = false
    )
    private Exhibition exhibition;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(
            name = "REQUEST_STATUS",
            nullable = false,
            length = 20
    )
    private StaffRequestStatus requestStatus = StaffRequestStatus.PENDING;

    @Generated(event = EventType.INSERT)
    @ColumnDefault("SYSDATE")
    @Column(
            name = "REQUESTED_AT",
            nullable = false,
            updatable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime requestedAt;

    @Column(
            name = "PROCESSED_AT",
            columnDefinition = "DATE"
    )
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESSED_BY")
    private Member processedBy;

    @PrePersist
    public void prePersist() {
        if (this.requestStatus == null) {
            this.requestStatus = StaffRequestStatus.PENDING;
        }
    }

    public void approve(
            Member processedBy,
            LocalDateTime processedAt
    ) {
        this.requestStatus = StaffRequestStatus.APPROVED;
        this.processedBy = processedBy;
        this.processedAt = processedAt;
    }

    public void reject(
            Member processedBy,
            LocalDateTime processedAt
    ) {
        this.requestStatus = StaffRequestStatus.REJECTED;
        this.processedBy = processedBy;
        this.processedAt = processedAt;
    }
}