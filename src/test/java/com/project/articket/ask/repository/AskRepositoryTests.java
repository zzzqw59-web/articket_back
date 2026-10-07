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


}