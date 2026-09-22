package com.project.articket.askReply.entity;

import com.project.articket.ask.entity.Ask;
import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ASK_REPLY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AskReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASK_REPLY_ID")
    private Long askReplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASK_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // DB 레벨 ON DELETE CASCADE
    private Ask askId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member memberId;

    @Column(name = "ASK_REPLY_BODY", length = 255, nullable = false)
    private String askReplyBody;

    @CreationTimestamp
    @Column(name = "ASK_REPLY_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime askReplyCreatedAt;

    @UpdateTimestamp
    @Column(name = "ASK_REPLY_MODIFIED_AT", nullable = false)
    private LocalDateTime askReplyModifiedAt;
}
