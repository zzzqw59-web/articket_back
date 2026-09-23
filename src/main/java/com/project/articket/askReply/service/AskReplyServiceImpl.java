package com.project.articket.askReply.service;

import com.project.articket.ask.entity.Ask;
import com.project.articket.ask.repository.AskRepository;
import com.project.articket.askReply.dto.AskReplyDTO;
import com.project.articket.askReply.dto.AskReplyRequestDTO;
import com.project.articket.askReply.entity.AskReply;
import com.project.articket.askReply.repository.AskReplyRepository;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.common.enums.MemberRole;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskReplyServiceImpl implements AskReplyService {

    private final AskReplyRepository askReplyRepository;
    private final AskRepository askRepository;
    private final MemberRepository memberRepository;

    // 1. 댓글 등록
    //[권한 제어]
    // - 관리자
    // - 또는 문의글에 exhibitionId가 존재할 때, 해당 전시의 담당자(STAFF)
    @Override
    @Transactional
    public Long createReply(Long askId, Long memberId, String loginMemberType, AskReplyRequestDTO requestDto) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. askId=" + askId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));

        // --- 권한 검증 ---
        boolean isAdmin = MemberRole.ADMIN.equalsKey(loginMemberType);
        boolean isExhibitionManager = false;

        /*
         * TODO: [전시 담당자 권한 검증 연동]
         * - 문의글에 전시 정보(ask.getExhibitionId())가 존재하는지 확인
         * - 팀원의 '전시 담당자(ExhibitionManager)' 엔티티/Repository 구현 완료 후
         *   해당 전시의 담당자 목록에 현재 loginMemberId가 포함되어 있는지 검증 logic 연결
         *
         * 예시 코드:
         * if (ask.getExhibitionId() != null) {
         *     Long exhibitionId = ask.getExhibitionId().getExhibitionId();
         *     isExhibitionManager = exhibitionManagerRepository
         *             .existsByExhibition_ExhibitionIdAndMember_MemberId(exhibitionId, memberId);
         * }
         */

        if (!isAdmin && !isExhibitionManager) {
            throw new IllegalStateException("답변(댓글) 작성 권한이 없습니다. 관리자 또는 해당 전시 담당자만 작성 가능합니다.");
        }

        AskReply askReply = requestDto.toEntity(ask, member);
        AskReply savedReply = askReplyRepository.save(askReply);

        return savedReply.getAskReplyId();
    }

    // 3. 댓글 조회
    @Override
    public PageResponseDTO<AskReplyDTO> getReplyList(Long askId, PageRequestDTO pageRequestDTO) {
        // 정렬 기준 필드 "askReplyId" 기준 DESC 정렬 (PageRequestDTO 활용)
        Pageable pageable = pageRequestDTO.getPageable("askReplyId");

        // Spring Data JPA의 findByAskId_AskId(askId, pageable) 레포지토리 메서드 호출
        Page<AskReply> replyPage = askReplyRepository.findByAskId_AskId(askId, pageable);

        List<AskReplyDTO> dtoList = replyPage.getContent().stream()
                .map(AskReplyDTO::from)
                .toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, replyPage.getTotalElements());
    }

    // 3. 댓글/답변 수정
    @Override
    @Transactional
    public void updateReply(Long askReplyId, Long memberId, AskReplyRequestDTO requestDto) {
        AskReply askReply = askReplyRepository.findById(askReplyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. askReplyId=" + askReplyId));

        // 본인이 작성한 댓글/답변만 수정 가능
        if (!askReply.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        askReply.updateReplyBody(requestDto.getAskReplyBody());
    }

    // 3. 댓글/답변 삭제
    @Override
    @Transactional
    public void deleteReply(Long askReplyId, Long memberId, String loginMemberType) {
        AskReply askReply = askReplyRepository.findById(askReplyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. askReplyId=" + askReplyId));

        boolean isOwner = askReply.getMemberId().getMemberId().equals(memberId);
        boolean isAdmin = MemberRole.ADMIN.equalsKey(loginMemberType);

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }

        askReplyRepository.delete(askReply);
    }

    // 마이페이지에서 내 댓글 전체 조회
    @Override
    public PageResponseDTO<AskReplyDTO> getMyReplyList(Long memberId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("askReplyId");
        Page<AskReply> replyPage = askReplyRepository.findMyReplies(memberId, pageable);

        List<AskReplyDTO> dtoList = replyPage.getContent().stream()
                .map(AskReplyDTO::from)
                .toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, replyPage.getTotalElements());
    }
}