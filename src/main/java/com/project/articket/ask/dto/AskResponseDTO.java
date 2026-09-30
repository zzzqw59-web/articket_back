package com.project.articket.ask.dto;

import com.project.articket.ask.entity.Ask;
import com.project.articket.askImage.entity.AskImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static com.project.articket.common.util.DateTimeUtils.toDateTimeSecondString;

@Getter
@Builder
public class AskResponseDTO {

    private String memberNickname;
    private String memberType;          // 작성자 권한/타입 (MemberRole)
    private String exhibitionTitle;
    private String askTitle;
    private String askBody;
    private Integer askType;
    private Integer askSecret;
    private Long askHits;
    private String askCreatedAt;
    private String askModifiedAt;

    private List<AskImageDTO> images;   // ★ 이미지 목록 (유지)

    // --- 1. 이미지 응답 DTO (유지) ---
    @Getter
    @Builder
    public static class AskImageDTO {
        private String askImageOrigin;   // 원본 첨부파일명
        private String imageUrl;         // 원본 이미지 접근 URL (/api/files/UUID_xxx.jpg)
        private String thumbnailUrl;     // 썸네일 이미지 접근 URL (/api/files/s_UUID_xxx.jpg)
        private Integer askImageOrder;   // 이미지 순서

        public static AskImageDTO from(AskImage image) {
            if (image == null) return null;

            String filename = image.getAskImageFilename();
            String originalUrl = image.getAskImageUrl();

            // askImageUrl에서 파일명(filename) 부분을 "s_" + filename 으로 치환하여 썸네일 URL 생성
            String thumbUrl = (originalUrl != null && filename != null && originalUrl.contains(filename))
                    ? originalUrl.replace(filename, "s_" + filename)
                    : null;

            return AskImageDTO.builder()
                    .askImageOrigin(image.getAskImageOrigin())
                    .imageUrl(originalUrl)
                    .thumbnailUrl(thumbUrl)
                    .askImageOrder(image.getAskImageOrder())
                    .build();
        }
    }

    // --- 정적 팩토리 메서드 ---
    public static AskResponseDTO from(Ask ask, List<AskImage> images) {
        LocalDateTime createdAt = ask.getAskCreatedAt();
        LocalDateTime modifiedAt = ask.getAskModifiedAt();

        // 등록 시간과 수정 시간이 같으면(수정된 적 없으면) null 처리
        boolean isModified = modifiedAt != null && !modifiedAt.equals(createdAt);

        return AskResponseDTO.builder()
                .memberNickname(ask.getMemberId().getMemberNickname())
                .memberType(ask.getMemberId().getMemberType())
                .exhibitionTitle(ask.getExhibitionId() != null ? ask.getExhibitionId().getExhibitionTitle() : null)
                .askTitle(ask.getAskTitle())
                .askBody(ask.getAskBody())
                .askType(ask.getAskType())
                .askSecret(ask.getAskSecret())
                .askHits(ask.getAskHits())
                .askCreatedAt(toDateTimeSecondString(ask.getAskCreatedAt()))
                .askModifiedAt(isModified ? toDateTimeSecondString(ask.getAskModifiedAt()) : null)
                .images(images != null ? images.stream().map(AskImageDTO::from).toList() : List.of())
                .build();
    }
}