package com.project.articket.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;

    public static final String TYPE_MEMBER = "MEMBER";
    public static final String TYPE_STAFF = "STAFF";
    public static final String TYPE_ADMIN = "ADMIN";

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

    @Column(
            name = "MEMBER_EMAIL",
            nullable = false,
            unique = true,
            length = 255
    )
    private String memberEmail;

    @Column(
            name = "MEMBER_PASSWORD",
            nullable = false,
            length = 255
    )
    private String memberPassword;

    @Column(
            name = "MEMBER_NICKNAME",
            nullable = false,
            length = 50
    )
    private String memberNickname;

    @Column(
            name = "MEMBER_NAME",
            nullable = false,
            length = 255
    )
    private String memberName;

    @Column(
            name = "MEMBER_PHONE",
            nullable = false,
            unique = true,
            length = 255
    )
    private String memberPhone;

    @Column(
            name = "MEMBER_TYPE",
            nullable = false,
            length = 20
    )
    private String memberType;

    @Column(
            name = "MEMBER_STATUS",
            nullable = false
    )
    private Integer memberStatus;

    @Generated(event = EventType.INSERT)
    @ColumnDefault("SYSDATE")
    @Column(
            name = "MEMBER_JOIN_CREATED_AT",
            nullable = false,
            updatable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime memberJoinCreatedAt;

    @PrePersist
    public void prePersist() {
        if (this.memberStatus == null) {
            this.memberStatus = STATUS_ACTIVE;
        }
    }

    public void updateNickname(String memberNickname) {
        this.memberNickname = memberNickname;
    }

    public void updatePassword(String memberPassword) {
        this.memberPassword = memberPassword;
    }

    public void updatePhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public void deactivate() {
        this.memberStatus = STATUS_INACTIVE;
    }

    public void activate() {
        this.memberStatus = STATUS_ACTIVE;
    }

    public void anonymize(
            String memberEmail,
            String memberPassword,
            String memberNickname,
            String memberName,
            String memberPhone
    ) {
        this.memberEmail = memberEmail;
        this.memberPassword = memberPassword;
        this.memberNickname = memberNickname;
        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.memberStatus = STATUS_INACTIVE;
    }
}