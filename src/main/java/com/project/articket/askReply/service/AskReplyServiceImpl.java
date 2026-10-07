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
import com.project.articket.common.util.NotificationManger;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.notification.dto.NotificationCreateDTO;
import com.project.articket.notification.service.NotificationService;
import com.project.articket.staff.service.StaffAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskReplyServiceImpl
        implements AskReplyService {

    private final AskReplyRepository askReplyRepository;
    private final AskRepository askRepository;
    private final MemberRepository memberRepository;
    private final NotificationManger notificationManager;
    private final StaffAuthorizationService staffAuthorizationService;

    // 1. 댓글 등록
    // [권한 제어]
    // - 관리자(ADMIN)
    // - 또는 문의글에 exhibitionId가 존재할 때, 해당 전시의 담당자(STAFF)
    @Override
    @Transactional
    public Long createReply(
            Long askId,
            Long memberId,
            AskReplyRequestDTO requestDto
    ) {

        Ask ask =
                askRepository.findById(askId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 문의글입니다. askId="
                                                + askId
                                )
                        );

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 회원입니다. memberId="
                                                + memberId
                                )
                        );

        boolean isAdmin =
                MemberRole.ADMIN.equalsKey(
                        member.getMemberType()
                );

        boolean isExhibitionManager =
                false;

        if (MemberRole.STAFF.equalsKey(
                member.getMemberType()
        )
                && ask.getExhibitionId()
                != null) {

            isExhibitionManager =
                    staffAuthorizationService
                            .hasExhibitionAuthority(
                                    memberId,
                                    ask.getExhibitionId()
                                            .getExhibitionId()
                            );
        }

        if (!isAdmin
                && !isExhibitionManager) {

            throw new IllegalStateException(
                    "답변(댓글) 작성 권한이 없습니다. 관리자 또는 해당 전시 담당자만 작성 가능합니다."
            );
        }

        AskReply askReply =
                requestDto.toEntity(
                        ask,
                        member
                );

        AskReply savedReply =
                askReplyRepository.save(
                        askReply
                );

        notificationManager.notifyUser(
                ask.getMemberId(),
                2,
                askId,
                memberId
        );

        return savedReply
                .getAskReplyId();
    }

    // 2. 댓글 조회
    @Override
    public PageResponseDTO<AskReplyDTO> getReplyList(
            Long askId,
            Long loginMemberId,
            PageRequestDTO pageRequestDTO
    ) {

        Ask ask =
                askRepository.findById(askId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 문의글입니다. askId="
                                                + askId
                                )
                        );

        if (ask.getAskSecret() != null
                && ask.getAskSecret() == 1) {

            String loginMemberType =
                    null;

            if (loginMemberId != null) {

                Member loginMember =
                        memberRepository
                                .findById(
                                        loginMemberId
                                )
                                .orElse(null);

                if (loginMember != null) {
                    loginMemberType =
                            loginMember
                                    .getMemberType();
                }
            }

            boolean isOwner =
                    loginMemberId != null
                            && loginMemberId.equals(
                            ask.getMemberId()
                                    .getMemberId()
                    );

            boolean isAdmin =
                    MemberRole.ADMIN.equalsKey(
                            loginMemberType
                    );

            boolean isExhibitionManager =
                    false;

            if (loginMemberId != null
                    && MemberRole.STAFF.equalsKey(
                    loginMemberType
            )
                    && ask.getExhibitionId()
                    != null) {

                isExhibitionManager =
                        staffAuthorizationService
                                .hasExhibitionAuthority(
                                        loginMemberId,
                                        ask.getExhibitionId()
                                                .getExhibitionId()
                                );
            }

            if (!isOwner
                    && !isAdmin
                    && !isExhibitionManager) {

                throw new IllegalStateException(
                        "해당 비밀글의 댓글을 열람할 권한이 없습니다."
                );
            }
        }

        Pageable pageable =
                pageRequestDTO
                        .getPageable(
                                "askReplyId"
                        );

        Page<AskReply> replyPage =
                askReplyRepository
                        .findByAskId_AskId(
                                askId,
                                pageable
                        );

        List<AskReplyDTO> dtoList =
                replyPage
                        .getContent()
                        .stream()
                        .map(
                                AskReplyDTO::from
                        )
                        .toList();

        return new PageResponseDTO<>(
                dtoList,
                pageRequestDTO,
                replyPage.getTotalElements()
        );
    }

    // 3. 댓글/답변 수정
    @Override
    @Transactional
    public void updateReply(
            Long askReplyId,
            Long memberId,
            AskReplyRequestDTO requestDto
    ) {

        AskReply askReply =
                askReplyRepository
                        .findById(
                                askReplyId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 댓글입니다. askReplyId="
                                                + askReplyId
                                )
                        );

        if (!askReply
                .getMemberId()
                .getMemberId()
                .equals(memberId)) {

            throw new IllegalStateException(
                    "댓글 수정 권한이 없습니다."
            );
        }

        askReply.updateReplyBody(
                requestDto.getAskReplyBody()
        );
    }

    // 4. 문의 댓글 삭제
    // [권한 제어]
    // - 댓글 작성자 본인
    // - 또는 관리자(ADMIN)
    @Override
    @Transactional
    public void deleteReply(
            Long replyId,
            Long memberId
    ) {

        AskReply askReply =
                askReplyRepository
                        .findById(replyId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 댓글입니다. replyId="
                                                + replyId
                                )
                        );

        Member requester =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 회원입니다. memberId="
                                                + memberId
                                )
                        );

        boolean isWriter =
                askReply.getMemberId()
                        .getMemberId()
                        .equals(memberId);

        boolean isAdmin =
                MemberRole.ADMIN.equalsKey(
                        requester.getMemberType()
                );

        if (!isWriter
                && !isAdmin) {

            throw new IllegalStateException(
                    "댓글 삭제 권한이 없습니다. 작성자 본인 또는 관리자만 삭제 가능합니다."
            );
        }

        askReplyRepository.delete(
                askReply
        );
    }

    // 5. 마이페이지에서 내 댓글 전체 조회
    @Override
    public PageResponseDTO<AskReplyDTO> getMyReplyList(
            Long memberId,
            PageRequestDTO pageRequestDTO
    ) {

        Pageable pageable =
                pageRequestDTO.getPageable(
                        "askReplyId"
                );

        Page<AskReply> replyPage =
                askReplyRepository
                        .findMyReplies(
                                memberId,
                                pageable
                        );

        List<AskReplyDTO> dtoList =
                replyPage
                        .getContent()
                        .stream()
                        .map(
                                AskReplyDTO::from
                        )
                        .toList();

        return new PageResponseDTO<>(
                dtoList,
                pageRequestDTO,
                replyPage.getTotalElements()
        );
    }
}