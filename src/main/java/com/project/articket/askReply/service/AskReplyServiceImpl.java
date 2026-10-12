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
import com.project.articket.common.util.NotificationManager;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.notification.enums.NotificationType;
import com.project.articket.staff.repository.StaffRepository;
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
    private final StaffRepository staffRepository;
    private final NotificationManager notificationManager;

    @Override
    @Transactional
    public Long createReply(Long askId, Long memberId, AskReplyRequestDTO requestDto) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. askId=" + askId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));

        boolean isAdmin = MemberRole.ADMIN.equalsKey(member.getMemberType());
        boolean isExhibitionManager = false;

        if (ask.getExhibitionId() != null) {
            Long exhibitionId = ask.getExhibitionId().getExhibitionId();
            isExhibitionManager = staffRepository.existsByMemberMemberIdAndExhibitionExhibitionId(
                    memberId,
                    exhibitionId
            );
        }

        if (!isAdmin && !isExhibitionManager) {
            throw new IllegalStateException("답변(댓글) 작성 권한이 없습니다. 관리자 또는 해당 전시 담당자만 작성 가능합니다.");
        }

        AskReply askReply = requestDto.toEntity(ask, member);
        AskReply savedReply = askReplyRepository.save(askReply);

        notificationManager.notifyUser(
                ask.getMemberId(),
                NotificationType.ASK_REPLY.getCode(),
                askId,
                memberId
        );

        return savedReply.getAskReplyId();
    }

    @Override
    public PageResponseDTO<AskReplyDTO> getReplyList(Long askId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("askReplyId");
        Page<AskReply> replyPage = askReplyRepository.findByAskId_AskId(askId, pageable);

        List<AskReplyDTO> dtoList = replyPage.getContent().stream()
                .map(AskReplyDTO::from)
                .toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, replyPage.getTotalElements());
    }

    @Override
    @Transactional
    public void updateReply(Long askReplyId, Long memberId, AskReplyRequestDTO requestDto) {
        AskReply askReply = askReplyRepository.findById(askReplyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. askReplyId=" + askReplyId));

        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));

        boolean isWriter = askReply.getMemberId().getMemberId().equals(memberId);
        boolean isAdmin = MemberRole.ADMIN.equalsKey(requester.getMemberType());

        if (!isWriter && !isAdmin) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        askReply.updateReplyBody(requestDto.getAskReplyBody());

        // 💡 [관리자에 의한 문의 댓글 수정 시 ASK_REPLY_UPDATE(14) 알림 발송]
        if (isAdmin && !isWriter) {
            notificationManager.notifyUser(
                    askReply.getAskId().getMemberId(),
                    NotificationType.ASK_REPLY_UPDATE.getCode(),
                    askReply.getAskId().getAskId(),
                    memberId
            );
        }
    }

    @Override
    @Transactional
    public void deleteReply(Long replyId, Long memberId) {
        AskReply askReply = askReplyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. replyId=" + replyId));

        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));

        boolean isWriter = askReply.getMemberId().getMemberId().equals(memberId);
        boolean isAdmin = MemberRole.ADMIN.equalsKey(requester.getMemberType());

        if (!isWriter && !isAdmin) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다. 작성자 본인 또는 관리자만 삭제 가능합니다.");
        }

        // 💡 [관리자에 의한 문의 댓글 삭제 시 ASK_REPLY_DELETE(15) 알림 발송]
        if (isAdmin && !isWriter) {
            notificationManager.notifyUser(
                    askReply.getAskId().getMemberId(),
                    NotificationType.ASK_REPLY_DELETE.getCode(),
                    askReply.getAskId().getAskId(),
                    memberId
            );
        }

        askReplyRepository.delete(askReply);
    }

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