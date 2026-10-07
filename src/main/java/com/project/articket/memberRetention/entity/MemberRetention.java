package com.project.articket.memberRetention.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER_RETENTION")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRetention {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "MEMBER_RETENTION_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "MEMBER_RETENTION_SEQ_GEN",
            sequenceName = "MEMBER_RETENTION_SEQ",
            allocationSize = 1
    )
    @Column(name = "RETENTION_ID")
    private Long retentionId;

    @Column(
            name = "ORIGINAL_MEMBER_ID",
            nullable = false
    )
    private Long originalMemberId;

    @Column(
            name = "MEMBER_NAME",
            nullable = false,
            length = 255
    )
    private String memberName;

    @Column(
            name = "MEMBER_EMAIL",
            nullable = false,
            length = 255
    )
    private String memberEmail;

    @Column(
            name = "MEMBER_PHONE",
            nullable = false,
            length = 255
    )
    private String memberPhone;

    @Column(
            name = "WITHDRAWN_AT",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime withdrawnAt;

    @Column(
            name = "FINAL_DESTROY_AT",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime finalDestroyAt;
}