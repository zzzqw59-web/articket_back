package com.project.articket.ask.dto;

import com.project.articket.ask.entity.Ask;
import com.project.articket.askImage.entity.AskImage;
import com.project.articket.askReply.entity.AskReply;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static com.project.articket.common.util.DateTimeUtils.toDateTimeSecondString;

@Getter
@Builder
public class AskResponseDTO {

    private String memberNickname;
    private String exhibitionTitle;
    private String askTitle;
    private String askBody;
    private Integer askType;
    private Integer askSecret;
    private Long askHits;
    private String askCreatedAt;
    private String askModifiedAt;

    private List<AskImageDTO> images;   // 이미지 목록
    private List<AskReplyDTO> replies;  // 댓글/답변 목록

    // --- 1. 이미지 응답 DTO ---
    @Getter
    @Builder
    public static class AskImageDTO {
        private String askImageOrigin;   // 원본 첨부파일명
        private String imageUrl;         // 원본 이미지 접근 URL (/api/asks/images/UUID_xxx.jpg)
        private String thumbnailUrl;     // 썸네일 이미지 접근 URL (/api/asks/images/s_UUID_xxx.jpg)
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

    // --- 2. 댓글/답변 응답 DTO ---
    @Getter
    @Builder
    public static class AskReplyDTO {
        private String memberNickname;      // 댓글 작성자 닉네임
        private String askReplyBody;        // 댓글 내용
        private String askReplyCreatedAt;
        private String askReplyModifiedAt;

        public static AskReplyDTO from(AskReply reply) {
            LocalDateTime createdAt = reply.getAskReplyCreatedAt();
            LocalDateTime modifiedAt = reply.getAskReplyModifiedAt();

            // 등록 시간과 수정 시간이 같으면(수정된 적 없으면) null 처리
            boolean isModified = modifiedAt != null && !modifiedAt.equals(createdAt);

            return AskReplyDTO.builder()
                    .memberNickname(reply.getMemberId().getMemberNickname())
                    .askReplyBody(reply.getAskReplyBody()) // Entity 필드명에 맞게 조정 가능
                    .askReplyCreatedAt(toDateTimeSecondString(reply.getAskReplyCreatedAt()))
                    .askReplyModifiedAt(isModified ? toDateTimeSecondString(reply.getAskReplyModifiedAt()): null)
                    .build();
        }
    }

    // --- 정적 팩토리 메서드 ---
    public static AskResponseDTO from(Ask ask, List<AskImage> images, List<AskReply> replies) {
        LocalDateTime createdAt = ask.getAskCreatedAt();
        LocalDateTime modifiedAt = ask.getAskModifiedAt();

        // 등록 시간과 수정 시간이 같으면(수정된 적 없으면) null 처리
        boolean isModified = modifiedAt != null && !modifiedAt.equals(createdAt);

        return AskResponseDTO.builder()
                .memberNickname(ask.getMemberId().getMemberNickname())
                .exhibitionTitle(ask.getExhibitionId() != null ? ask.getExhibitionId().getExhibitionTitle() : null)
                .askTitle(ask.getAskTitle())
                .askBody(ask.getAskBody())
                .askType(ask.getAskType())
                .askSecret(ask.getAskSecret())
                .askHits(ask.getAskHits())
                .askCreatedAt(toDateTimeSecondString(ask.getAskCreatedAt()))
                .askModifiedAt(isModified ? toDateTimeSecondString(ask.getAskModifiedAt()) : null)
                .images(images != null ? images.stream().map(AskImageDTO::from).toList() : List.of())
                .replies(replies != null ? replies.stream().map(AskReplyDTO::from).toList() : List.of())
                .build();
    }
}