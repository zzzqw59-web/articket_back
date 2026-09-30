package com.project.articket.withdraw.entity;

import com.project.articket.member.entity.Member;
import com.project.articket.withdraw.enums.WithdrawStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "WITHDRAW")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Withdraw {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "WITHDRAW_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "WITHDRAW_SEQ_GEN",
            sequenceName = "WITHDRAW_SEQ",
            allocationSize = 1
    )
    @Column(name = "WITHDRAW_ID")
    private Long withdrawId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "MEMBER_ID",
            nullable = false
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "WITHDRAW_STATUS",
            nullable = false,
            length = 20
    )
    private WithdrawStatus withdrawStatus;

    @Generated(event = EventType.INSERT)
    @ColumnDefault("SYSDATE")
    @Column(
            name = "WITHDRAW_CREATED_AT",
            nullable = false,
            updatable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime withdrawCreatedAt;

    @Column(
            name = "WITHDRAW_CANCELED_AT",
            columnDefinition = "DATE"
    )
    private LocalDateTime withdrawCanceledAt;

    @Column(
            name = "WITHDRAWN_AT",
            columnDefinition = "DATE"
    )
    private LocalDateTime withdrawnAt;

    @Column(
            name = "WITHDRAW_DUE",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime withdrawDue;
}