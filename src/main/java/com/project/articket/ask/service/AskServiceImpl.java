package com.project.articket.ask.service;

import com.project.articket.ask.dto.AskCreateRequestDTO;
import com.project.articket.ask.dto.AskListResponseDTO;
import com.project.articket.ask.dto.AskResponseDTO;
import com.project.articket.ask.dto.AskUpdateRequestDTO;
import com.project.articket.ask.entity.Ask;
import com.project.articket.ask.repository.AskRepository;
import com.project.articket.askImage.entity.AskImage;
import com.project.articket.askImage.repository.AskImageRepository;
import com.project.articket.askReply.entity.AskReply;
import com.project.articket.askReply.repository.AskReplyRepository;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.common.enums.MemberRole;
import com.project.articket.common.util.CustomFileUtil;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CustomFileUtil fileUtil;

    // 1. 문의글 작성 (파일 업로드 포함)
    @Override
    @Transactional
    public Long createAsk(Long memberId, AskCreateRequestDTO requestDto, List<MultipartFile> files) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));

        Exhibition exhibition = null;
        if (requestDto.getExhibitionId() != null) {
            exhibition = exhibitionRepository.findById(requestDto.getExhibitionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전시입니다. exhibitionId=" + requestDto.getExhibitionId()));
        }

        Ask ask = requestDto.toEntity(member, exhibition);
        Ask savedAsk = askRepository.save(ask);

        if (files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file != null && !file.isEmpty()) {
                    String savedFilename = fileUtil.saveFile(file);

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

    // 2. 문의글 목록 조회
    @Override
    public PageResponseDTO<AskListResponseDTO> getAskList(String searchType, String keyword, Integer askType, Long loginMemberId, String loginMemberType, String sort, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("askId");

        Page<Ask> askPage = askRepository.searchAsks(searchType, keyword, askType, loginMemberId, loginMemberType, sort, pageable);

        List<AskListResponseDTO> dtoList = askPage.getContent().stream()
                .map(AskListResponseDTO::from) // 단일 인자 from 메서드 호출로 정상화
                .toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, askPage.getTotalElements());
    }

    // 3. 문의글 상세 조회 (MemberRole Enum 적용 및 TODO 보완)
    @Override
    @Transactional
    public AskResponseDTO getAskDetail(Long askId, Long loginMemberId, String loginMemberType) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. askId=" + askId));

        // 비밀글 권한 체크
        if (ask.getAskSecret() != null && ask.getAskSecret() == 1) {
            boolean isOwner = loginMemberId != null && loginMemberId.equals(ask.getMemberId().getMemberId());
            boolean isAdmin = MemberRole.ADMIN.equalsKey(loginMemberType);
            boolean isExhibitionManager = false;

            /*
             * TODO: [전시 담당자 상세 조회 권한 연동]
             * if (ask.getExhibitionId() != null) {
             *     isExhibitionManager = exhibitionManagerRepository
             *             .existsByExhibition_ExhibitionIdAndMember_MemberId(ask.getExhibitionId().getExhibitionId(), loginMemberId);
             * }
             */

            if (!isOwner && !isAdmin && !isExhibitionManager) {
                throw new IllegalStateException("해당 비밀글을 열람할 권한이 없습니다.");
            }
        }

        ask.increaseHits();

        List<AskImage> images = askImageRepository.findByAskId_AskId(askId);

        return AskResponseDTO.from(ask, images);
    }

    // 4. 문의글 수정
    @Override
    @Transactional
    public Long updateAsk(Long askId, Long memberId, AskUpdateRequestDTO requestDto, List<MultipartFile> newFiles) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. askId=" + askId));

        if (!ask.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        Exhibition exhibition = null;
        if (requestDto.getExhibitionId() != null) {
            exhibition = exhibitionRepository.findById(requestDto.getExhibitionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전시입니다. exhibitionId=" + requestDto.getExhibitionId()));
        }

        ask.updateAsk(
                requestDto.getAskTitle(),
                requestDto.getAskBody(),
                requestDto.getAskType(),
                requestDto.getAskSecret(),
                exhibition
        );

        List<AskImage> currentImages = askImageRepository.findByAskId_AskId(askId);
        List<String> keepFilenames = requestDto.getKeepImageFilenames() != null
                ? requestDto.getKeepImageFilenames()
                : List.of();

        for (AskImage currentImage : currentImages) {
            if (!keepFilenames.contains(currentImage.getAskImageFilename())) {
                fileUtil.deleteFile(currentImage.getAskImageFilename());
                askImageRepository.delete(currentImage);
            }
        }

        List<AskImage> remainingImages = askImageRepository.findByAskId_AskId(askId);
        int currentOrder = remainingImages.size() + 1;

        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (file != null && !file.isEmpty()) {
                    String savedFilename = fileUtil.saveFile(file);

                    AskImage newAskImage = AskImage.createAskImage(
                            ask,
                            file.getOriginalFilename(),
                            savedFilename,
                            currentOrder++
                    );

                    askImageRepository.save(newAskImage);
                }
            }
        }

        return ask.getAskId();
    }

    // 5. 문의글 삭제
    @Override
    @Transactional
    public void deleteAsk(Long askId, Long memberId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. askId=" + askId));

        if (!ask.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        List<AskImage> images = askImageRepository.findByAskId_AskId(askId);
        for (AskImage image : images) {
            fileUtil.deleteFile(image.getAskImageFilename());
        }

        askRepository.delete(ask);
    }
}