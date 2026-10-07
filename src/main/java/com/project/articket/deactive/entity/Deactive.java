package com.project.articket.deactive.entity;

import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "DEACTIVE")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deactive {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "DEACTIVE_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "DEACTIVE_SEQ_GEN",
            sequenceName = "DEACTIVE_SEQ",
            allocationSize = 1
    )
    @Column(name = "DEACTIVE_ID")
    private Long deactiveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "MEMBER_ID",
            nullable = false
    )
    private Member member;

    @Column(
            name = "DEACTIVE_BODY",
            nullable = false,
            length = 255
    )
    private String deactiveBody;

    @Generated(event = EventType.INSERT)
    @ColumnDefault("SYSDATE")
    @Column(
            name = "DEACTIVE_CREATED_AT",
            nullable = false,
            updatable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime deactiveCreatedAt;

    @Column(
            name = "DEACTIVE_TERM",
            nullable = false
    )
    private Integer deactiveTerm;

    @Column(
            name = "DEACTIVE_CONFIRMED_AT",
            columnDefinition = "DATE"
    )
    private LocalDateTime deactiveConfirmedAt;

    public void confirm(
            LocalDateTime confirmedAt
    ) {

        if (this.deactiveConfirmedAt == null) {
            this.deactiveConfirmedAt = confirmedAt;
        }
    }
}