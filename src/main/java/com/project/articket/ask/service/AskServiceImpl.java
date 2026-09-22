package com.project.articket.ask.service;

import com.project.articket.ask.dto.AskCreateRequestDTO;
import com.project.articket.ask.dto.AskResponseDTO;
import com.project.articket.ask.entity.Ask;
import com.project.articket.ask.repository.AskRepository;
import com.project.articket.askImage.entity.AskImage;
import com.project.articket.askImage.repository.AskImageRepository;
import com.project.articket.askReply.entity.AskReply;
import com.project.articket.askReply.repository.AskReplyRepository;
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
public class AskServiceImpl implements AskService {

    private final AskRepository askRepository;
    private final AskImageRepository askImageRepository;
    private final AskReplyRepository askReplyRepository;
    private final MemberRepository memberRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final CustomFileUtil fileUtil; // 파일 저장/삭제 유틸리티

    /**
     * 1. 문의글 작성 (파일 업로드 포함)
     */
    @Override
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

        // 이미지 파일 저장 및 AskImage 엔티티 생성 처리
        if (files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file != null && !file.isEmpty()) {
                    // 1) CustomFileUtil을 통해 디렉터리에 실물 파일(원본+썸네일) 저장
                    String savedFilename = fileUtil.saveFile(file);

                    // 2) AskImage 정적 팩토리 메서드를 이용해 엔티티 생성 (/api/asks/images/ 주소 자동 구성)
                    AskImage askImage = AskImage.createAskImage(
                            savedAsk,
                            file.getOriginalFilename(),
                            savedFilename,
                            i + 1
                    );

                    askImageRepository.save(askImage);
                }
            }
        }

        return savedAsk.getAskId();
    }

    /**
     * 2. 문의글 상세 조회 (조회수 증가 + 이미지/댓글 포함 응답)
     */
    @Override
    @Transactional
    public AskResponseDTO getAskDetail(Long askId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. ID: " + askId));

        // 조회수 증가 (Ask 엔티티 내부의 null-safe increaseHits 호출)
        ask.increaseHits();

        // 연관 이미지 및 댓글 목록 조회
        List<AskImage> images = askImageRepository.findByAskId_AskId(askId);
        List<AskReply> replies = askReplyRepository.findByAskId_AskId(askId);

        // AskResponseDTO 팩토리 메서드를 통해 닉네임, 원본/썸네일 URL, 댓글 리스트까지 변환되어 전달
        return AskResponseDTO.from(ask, images, replies);
    }

    /**
     * 3. 문의글 삭제 (Hard Delete + 서버 실물 이미지 파일 삭제)
     */
    @Override
    @Transactional
    public void deleteAsk(Long askId, Long memberId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. ID: " + askId));

        // 작성자 검증
        if (!ask.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        // 1) 서버 저장소에서 연관 이미지 실물 파일(원본 및 s_ 썸네일) 일괄 삭제
        List<AskImage> images = askImageRepository.findByAskId_AskId(askId);
        for (AskImage image : images) {
            fileUtil.deleteFile(image.getAskImageFilename());
        }

        // 2) DB 레코드 삭제 (AskImage DB 레코드는 OnDelete CASCADE에 의해 자동 삭제)
        askRepository.delete(ask);
    }
}