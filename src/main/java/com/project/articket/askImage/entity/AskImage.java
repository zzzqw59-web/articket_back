package com.project.articket.askImage.entity;

import com.project.articket.ask.entity.Ask;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "ASK_IMAGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AskImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASK_IMAGE_ID")
    private Long askImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASK_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // DB 레벨 ON DELETE CASCADE
    private Ask askId;

    @Column(name = "ASK_IMAGE_ORIGIN", nullable = false)
    private String askImageOrigin;

    @Column(name = "ASK_IMAGE_FILENAME", nullable = false)
    private String askImageFilename;

    @Column(name = "ASK_IMAGE_URL", nullable = false)
    private String askImageUrl;

    @Column(name = "ASK_IMAGE_ORDER", nullable = false)
    private Integer askImageOrder;

    @CreationTimestamp
    @Column(name = "ASK_IMAGE_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime askImageCreatedAt;
}
