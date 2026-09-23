package com.project.articket.ask.repository;

import java.time.LocalDateTime;
import com.project.articket.ask.entity.Ask;
import com.project.articket.askImage.entity.AskImage;
import com.project.articket.askImage.repository.AskImageRepository;
import com.project.articket.askReply.entity.AskReply;
import com.project.articket.askReply.repository.AskReplyRepository;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false) // 테스트 완료 후 데이터베이스에 더미 데이터를 남기기 위해 false로 설정
class AskRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    @Autowired
    private AskRepository askRepository;

    @Autowired
    private AskImageRepository askImageRepository;

    @Autowired
    private AskReplyRepository askReplyRepository;

    @Test
    @DisplayName("회원, 전시, 문의글, 이미지, 댓글 더미 데이터 생성 및 조회 검증")
    void insertDummyDataAndVerify() {
        // 1. 더미 회원 생성 (일반 사용자 2명, 관리자 1명)
        Member member1 = Member.builder()
                .memberEmail("user1@articket.com")
                .memberPassword("password123!")
                .memberName("홍길동")
                .memberNickname("티켓왕")
                .memberType("일반회원")
                .memberPhone("010-1234-4567")
                .memberJoinCreatedAt(LocalDateTime.now())
                .memberStatus(0)
                .build();

        Member member2 = Member.builder()
                .memberEmail("user2@articket.com")
                .memberPassword("password123!")
                .memberName("김철수")
                .memberNickname("전시매니아")
                .memberType("일반회원")
                .memberPhone("010-1234-4568")
                .memberJoinCreatedAt(LocalDateTime.now())
                .memberStatus(0)
                .build();

        Member admin = Member.builder()
                .memberEmail("admin@articket.com")
                .memberPassword("admin123!")
                .memberName("관리자")
                .memberNickname("운영자")
                .memberType("관리자")
                .memberPhone("010-1234-4599")
                .memberJoinCreatedAt(LocalDateTime.now())
                .memberStatus(0)
                .build();

        memberRepository.saveAll(List.of(member1, member2, admin));

        // 2. 더미 전시 생성
        Exhibition exhibition = Exhibition.builder()
                .exhibitionTitle("반 고흐, 영혼의 편지 展")
                .build();

        exhibitionRepository.save(exhibition);

        // 3. 더미 문의글 생성 (일반 문의 1개, 전시 관련 문의 1개)
        Ask ask1 = Ask.builder()
                .memberId(member1)
                .exhibitionId(null) // 전시 미지정 일반 문의
                .askTitle("예약 취소 및 환불 규정이 궁금합니다.")
                .askBody("관람일 3일 전에 취소하면 100% 환불이 가능한가요?")
                .askType(1) // 1: 예매/결제 문의
                .askSecret(0) // 0: 공개글
                .askHits(0L)
                .build();

        Ask ask2 = Ask.builder()
                .memberId(member2)
                .exhibitionId(exhibition) // 전시 지정 문의
                .askTitle("반 고흐 展 오디오 가이드 대여 문의")
                .askBody("현장에서 오디오 가이드 대여 시 별도 비용이 드는지 궁금합니다.")
                .askType(2) // 2: 전시 관람 문의
                .askSecret(1) // 1: 비밀글
                .askHits(0L)
                .build();

        askRepository.saveAll(List.of(ask1, ask2));

        // 4. 더미 문의 이미지 생성
        String savedFilename1 = "c8f3b2a1_ticket_screen.png";
        AskImage image1 = AskImage.createAskImage(ask1, "티켓캡처.png", savedFilename1, 1);

        String savedFilename2 = "a9d8c7b6_exhibition_info.jpg";
        AskImage image2 = AskImage.createAskImage(ask2, "전시안내.jpg", savedFilename2, 1);

        askImageRepository.saveAll(List.of(image1, image2));

        // 5. 더미 문의 댓글(답변) 생성
        AskReply reply1 = AskReply.builder()
                .askId(ask1)
                .memberId(admin)
                .askReplyBody("안녕하세요. 관람일 3일 전 취소 시 수수료 없이 100% 환불 가능합니다.")
                .build();

        AskReply reply2 = AskReply.builder()
                .askId(ask2)
                .memberId(admin)
                .askReplyBody("오디오 가이드는 현장 안내데스크에서 3,000원에 대여 가능합니다.")
                .build();

        askReplyRepository.saveAll(List.of(reply1, reply2));

        // --------------------------------------------------
        // 6. DB 저장 결과 검증 (Repository 조회 테스트)
        // --------------------------------------------------

        // 문의글1 조회 및 연관 데이터 검증
        Ask foundAsk = askRepository.findById(ask1.getAskId()).orElseThrow();
        assertThat(foundAsk.getAskTitle()).isEqualTo("예약 취소 및 환불 규정이 궁금합니다.");
        assertThat(foundAsk.getMemberId().getMemberNickname()).isEqualTo("티켓왕");

        // 이미지 조회 검증
        List<AskImage> foundImages = askImageRepository.findByAskId_AskId(foundAsk.getAskId());
        assertThat(foundImages).hasSize(1);
        assertThat(foundImages.get(0).getAskImageUrl()).isEqualTo("/api/asks/images/" + savedFilename1);

        // 댓글 조회 검증
        List<AskReply> foundReplies = askReplyRepository.findByAskId_AskId(foundAsk.getAskId());
        assertThat(foundReplies).hasSize(1);
        assertThat(foundReplies.get(0).getMemberId().getMemberNickname()).isEqualTo("운영자");
        assertThat(foundReplies.get(0).getAskReplyBody()).contains("100% 환불 가능합니다");
    }
}