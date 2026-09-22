package com.project.articket.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "MEMBER_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "MEMBER_SEQ_GEN",
            sequenceName = "MEMBER_SEQ",
            allocationSize = 1
    )
    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Column(name = "MEMBER_EMAIL", nullable = false, unique = true, length = 255)
    private String memberEmail;

    @Column(name = "MEMBER_PASSWORD", nullable = false, length = 255)
    private String memberPassword;

    @Column(name = "MEMBER_NICKNAME", nullable = false, length = 50)
    private String memberNickname;

    @Column(name = "MEMBER_NAME", nullable = false, length = 50)
    private String memberName;

    @Column(name = "MEMBER_PHONE", nullable = false, unique = true, length = 255)
    private String memberPhone;

    @Column(name = "MEMBER_TYPE", nullable = false, length = 20)
    private String memberType;

    @Column(name = "MEMBER_STATUS", nullable = false)
    private Integer memberStatus;

    @Column(name = "MEMBER_JOIN_CREATED_AT", nullable = false)
    private LocalDateTime memberJoinCreatedAt;
}