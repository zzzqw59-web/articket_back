package com.project.articket.ask.service;

import com.project.articket.ask.dto.AskCreateRequestDTO;
import com.project.articket.ask.entity.Ask;
import com.project.articket.ask.repository.AskRepository;
import com.project.articket.askImage.entity.AskImage;
import com.project.articket.askImage.repository.AskImageRepository;
import com.project.articket.common.util.CustomFileUtil;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskServiceImpl implements AskService{
    private final AskRepository askRepository;
    private final AskImageRepository askImageRepository;
    private final MemberRepository memberRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final CustomFileUtil fileUtil; // 파일 저장/삭제 유틸리티 주입

    /**
     * 1. 문의글 작성 (파일 업로드 포함)
     */
    @Transactional
    public Long createAsk(Long memberId, AskCreateRequestDTO requestDto, List<MultipartFile> files) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));

        Exhibition exhibition = null;
        if (requestDto.getExhibitionId() != null) {
            exhibition = exhibitionRepository.findById(requestDto.getExhibitionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전시입니다. ID: " + requestDto.getExhibitionId()));
        }

        Ask ask = requestDto.toEntity(member, exhibition);
        Ask savedAsk = askRepository.save(ask);

        // 이미지 파일 저장 처리
        if (files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file != null && !file.isEmpty()) {
                    // 1) CustomFileUtil을 통해 실물 파일 저장
                    String savedFilename = fileUtil.saveFile(file);

                    // 2) AskImage 엔티티 생성 및 DB 저장
                    AskImage askImage = AskImage.builder()
                            .askId(savedAsk)
                            .askImageOrigin(file.getOriginalFilename())
                            .askImageFilename(savedFilename)
                            .askImageUrl(savedFilename) // 조회 컨트롤러 URL 규칙에 따라 변경 가능
                            .askImageOrder(i + 1)
                            .build();

                    askImageRepository.save(askImage);
                }
            }
        }

        return savedAsk.getAskId();
    }

    /**
     * 2. 문의글 삭제 (Hard Delete + 실물 이미지 파일 삭제)
     */
    @Transactional
    public void deleteAsk(Long askId, Long memberId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. ID: " + askId));

        if (!ask.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        // 1) 연관된 이미지 실물 파일 서버 디렉터리에서 삭제
        List<AskImage> images = askImageRepository.findByAskId_AskId(askId);
        for (AskImage image : images) {
            fileUtil.deleteFile(image.getAskImageFilename()); // 원본 및 썸네일 자동 삭제
        }

        // 2) 문의글 삭제 (엔티티의 @OnDelete(action = OnDeleteAction.CASCADE)에 의해 AskImage DB 레코드 자동 삭제)
        askRepository.delete(ask);
    }
}
