package com.project.articket.staff.entity;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "STAFF",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_STAFF_MEMBER_EXHIBITION",
                        columnNames = {
                                "MEMBER_ID",
                                "EXHIBITION_ID"
                        }
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Staff {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "STAFF_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "STAFF_SEQ_GEN",
            sequenceName = "STAFF_SEQ",
            allocationSize = 1
    )
    @Column(name = "STAFF_ID")
    private Long staffId;

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

    @Generated(event = EventType.INSERT)
    @ColumnDefault("SYSDATE")
    @Column(
            name = "STAFF_CREATED_AT",
            nullable = false,
            updatable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime staffCreatedAt;
}