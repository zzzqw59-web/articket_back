package com.project.articket.ask.dto;

import com.project.articket.ask.entity.Ask;
import com.project.articket.askImage.entity.AskImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AskResponseDTO {

    private Long askId;
    private Long memberId;
    private String memberName;
    private Long exhibitionId;
    private String exhibitionTitle;
    private String askTitle;
    private String askBody;
    private Integer askType;
    private Integer askSecret;
    private Long askHits;
    private LocalDateTime askCreatedAt;

    private List<AskImageDTO> images; // 이미지 상세 DTO 리스트

    @Getter
    @Builder
    public static class AskImageDTO {
        private Long askImageId;
        private String askImageOrigin;
        private String askImageFilename;
        private String thumbnailUrl; // s_ 접두사가 붙은 썸네일 파일명
        private Integer askImageOrder;

        public static AskImageDTO from(AskImage image) {
            return AskImageDTO.builder()
                    .askImageId(image.getAskImageId())
                    .askImageOrigin(image.getAskImageOrigin())
                    .askImageFilename(image.getAskImageFilename())
                    .thumbnailUrl("s_" + image.getAskImageFilename()) // CustomFileUtil의 썸네일 생성 규칙
                    .askImageOrder(image.getAskImageOrder())
                    .build();
        }
    }

    public static AskResponseDTO from(Ask ask, List<AskImage> images) {
        return AskResponseDTO.builder()
                .askId(ask.getAskId())
                .memberId(ask.getMemberId().getMemberId())
                .memberName(ask.getMemberId().getMemberName())
                .exhibitionId(ask.getExhibitionId() != null ? ask.getExhibitionId().getExhibitionId() : null)
                .exhibitionTitle(ask.getExhibitionId() != null ? ask.getExhibitionId().getExhibitionTitle() : null)
                .askTitle(ask.getAskTitle())
                .askBody(ask.getAskBody())
                .askType(ask.getAskType())
                .askSecret(ask.getAskSecret())
                .askHits(ask.getAskHits())
                .askCreatedAt(ask.getAskCreatedAt())
                .images(images.stream().map(AskImageDTO::from).toList())
                .build();
    }
}