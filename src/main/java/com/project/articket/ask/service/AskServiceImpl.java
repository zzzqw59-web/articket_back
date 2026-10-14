package com.project.articket.ask.service;

import com.project.articket.ask.dto.AskCreateRequestDTO;
import com.project.articket.ask.dto.AskListResponseDTO;
import com.project.articket.ask.dto.AskResponseDTO;
import com.project.articket.ask.dto.AskUpdateRequestDTO;
import com.project.articket.ask.entity.Ask;
import com.project.articket.ask.repository.AskRepository;
import com.project.articket.askImage.entity.AskImage;
import com.project.articket.askImage.repository.AskImageRepository;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.common.enums.MemberRole;
import com.project.articket.common.util.CustomFileUtil;
import com.project.articket.common.util.NotificationManager;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.notification.enums.NotificationType;
import com.project.articket.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AskServiceImpl implements AskService {

    private final AskRepository askRepository;
    private final AskImageRepository askImageRepository;
    private final MemberRepository memberRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final StaffRepository staffRepository;
    private final CustomFileUtil fileUtil;
    private final NotificationManager notificationManager;

    // 💡 [이미지 검증 공통 헬퍼 메서드] (최대 3장, 5MB, 허용 확장자: JPG, JPEG, PNG, GIF, WEBP)
    private void validateImages(List<MultipartFile> files, int existingCount) {
        if (files == null) return;

        List<MultipartFile> validFiles = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (existingCount + validFiles.size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3장까지만 업로드할 수 있습니다.");
        }

        for (MultipartFile file : validFiles) {
            // 용량 검증 (5MB = 5 * 1024 * 1024 bytes)
            if (file.getSize() > 5L * 1024 * 1024) {
                throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
            }

            // Content-Type 형식 검증
            String contentType = file.getContentType();
            if (contentType == null || !(
                    contentType.equals("image/jpeg") ||
                            contentType.equals("image/jpg") ||
                            contentType.equals("image/png") ||
                            contentType.equals("image/gif") ||
                            contentType.equals("image/webp")
            )) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (JPG, JPEG, PNG, GIF, WEBP만 가능합니다.)");
            }
        }
    }

    // 1. 문의글 작성 (파일 업로드 검증 및 보상 처리 포함)
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

        // 등록 시 이미지 검증 (기존 이미지 0장 기준)
        validateImages(files, 0);

        List<String> uploadedFilenames = new ArrayList<>();
        Ask savedAsk;

        try {
            Ask ask = requestDto.toEntity(member, exhibition);
            savedAsk = askRepository.save(ask);

            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    if (file != null && !file.isEmpty()) {
                        String savedFilename = fileUtil.saveFile(file);
                        uploadedFilenames.add(savedFilename);

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
        } catch (Exception e) {
            // 💡 [정합성 보상 처리] DB 저장이 실패하여 롤백될 때, 이미 디스크에 업로드된 파일들 회수(고아 파일 방지)
            for (String filename : uploadedFilenames) {
                try {
                    fileUtil.deleteFile(filename);
                } catch (Exception fileEx) {
                    log.error("업로드된 고아 파일 삭제 실패: {}", filename, fileEx);
                }
            }
            throw e;
        }

        // --- [관리자 및 전시 관계자 알림 발송] ---
        notificationManager.notifyAllAdmins(NotificationType.ASK_NEW.getCode(), savedAsk.getAskId());

        if (savedAsk.getExhibitionId() != null) {
            notificationManager.notifyExhibitionStaffs(
                    savedAsk.getExhibitionId().getExhibitionId(),
                    NotificationType.ASK_NEW.getCode(),
                    savedAsk.getAskId()
            );
        }

        return savedAsk.getAskId();
    }

    // 2. 문의글 목록 조회
    @Override
    public PageResponseDTO<AskListResponseDTO> getAskList(String searchType, String keyword, Integer askType, Long loginMemberId, String sort, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("askId");

        String resolvedMemberType = null;
        if (loginMemberId != null) {
            Member loginMember = memberRepository.findById(loginMemberId).orElse(null);
            if (loginMember != null) {
                resolvedMemberType = loginMember.getMemberType();
            }
        }

        Page<Ask> askPage = askRepository.searchAsks(searchType, keyword, askType, loginMemberId, resolvedMemberType, sort, pageable);

        List<AskListResponseDTO> dtoList = askPage.getContent().stream()
                .map(AskListResponseDTO::from)
                .toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, askPage.getTotalElements());
    }

    // 3. 문의글 상세 조회
    @Override
    @Transactional
    public AskResponseDTO getAskDetail(Long askId, Long loginMemberId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 문의글입니다. askId=" + askId));

        String loginMemberType = null;
        if (loginMemberId != null) {
            Member loginMember = memberRepository.findById(loginMemberId).orElse(null);
            if (loginMember != null) {
                loginMemberType = loginMember.getMemberType();
            }
        }

        // 비밀글 권한 체크 로직
        if (ask.getAskSecret() != null && ask.getAskSecret() == 1) {
            boolean isOwner = loginMemberId != null && loginMemberId.equals(ask.getMemberId().getMemberId());
            boolean isAdmin = MemberRole.ADMIN.equalsKey(loginMemberType);

            boolean isExhibitionManager = false;
            if (loginMemberId != null && MemberRole.STAFF.equalsKey(loginMemberType) && ask.getExhibitionId() != null) {
                isExhibitionManager = staffRepository.existsByMemberMemberIdAndExhibitionExhibitionId(
                        loginMemberId,
                        ask.getExhibitionId().getExhibitionId()
                );
            }

            if (!isOwner && !isAdmin && !isExhibitionManager) {
                throw new IllegalStateException("해당 비밀글을 열람할 권한이 없습니다.");
            }
        }

        ask.increaseHits();
        List<AskImage> images = askImageRepository.findByAskId_AskId(askId);

        return AskResponseDTO.from(ask, images);
    }

    // 4. 문의글 수정 (이미지 보상 처리 및 커밋 후 파일 삭제 동기화 반영)
    @Override
    @Transactional
    public Long updateAsk(Long askId, Long memberId, AskUpdateRequestDTO requestDto, List<MultipartFile> newFiles) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        boolean isWriter = ask.getMemberId().getMemberId().equals(memberId);
        boolean isAdmin = MemberRole.ADMIN.equalsKey(member.getMemberType());

        if (!isWriter && !isAdmin) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        // 기존 이미지 유지 목록 파악
        List<AskImage> currentImages = askImageRepository.findByAskId_AskId(askId);
        List<String> keepFilenames = requestDto.getKeepImageFilenames() != null
                ? requestDto.getKeepImageFilenames()
                : List.of();

        int keepCount = (int) currentImages.stream()
                .filter(img -> keepFilenames.contains(img.getAskImageFilename()))
                .count();

        // 새로 업로드하는 이미지 검증
        validateImages(newFiles, keepCount);

        // 문의 유형 및 연관 전시 기존 값 유지
        ask.updateAsk(
                requestDto.getAskTitle(),
                requestDto.getAskBody(),
                ask.getAskType(),
                requestDto.getAskSecret(),
                ask.getExhibitionId()
        );

        // 삭제 대상 파일명 목록 수집 (트랜잭션 커밋 직후에 삭제하도록 지연)
        List<String> filesToDelete = new ArrayList<>();
        for (AskImage currentImage : currentImages) {
            if (!keepFilenames.contains(currentImage.getAskImageFilename())) {
                filesToDelete.add(currentImage.getAskImageFilename());
                askImageRepository.delete(currentImage);
            }
        }

        List<String> uploadedNewFilenames = new ArrayList<>();
        try {
            List<AskImage> remainingImages = askImageRepository.findByAskId_AskId(askId);
            int currentOrder = remainingImages.size() + 1;

            if (newFiles != null && !newFiles.isEmpty()) {
                for (MultipartFile file : newFiles) {
                    if (file != null && !file.isEmpty()) {
                        String savedFilename = fileUtil.saveFile(file);
                        uploadedNewFilenames.add(savedFilename);

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
        } catch (Exception e) {
            // 수정 중 DB 에러 발생 시 새로 업로드했던 파일 회수
            for (String filename : uploadedNewFilenames) {
                try {
                    fileUtil.deleteFile(filename);
                } catch (Exception fileEx) {
                    log.error("수정 중 업로드된 고아 파일 삭제 실패: {}", filename, fileEx);
                }
            }
            throw e;
        }

        // 💡 [트랜잭션 정합성 보호] DB 트랜잭션이 성공적으로 커밋된 이후에만 실제 파일 삭제 실행 (롤백 시 파일 유실 방지)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String filename : filesToDelete) {
                    try {
                        fileUtil.deleteFile(filename);
                    } catch (Exception e) {
                        log.error("커밋 후 파일 삭제 실패: {}", filename, e);
                    }
                }
            }
        });

        // 관리자에 의한 수정 알림 발송
        if (isAdmin && !isWriter) {
            notificationManager.notifyUser(
                    ask.getMemberId(),
                    NotificationType.ASK_UPDATE.getCode(),
                    ask.getAskId(),
                    memberId
            );
        }

        return ask.getAskId();
    }

    // 5. 문의글 삭제 (커밋 후 파일 삭제 동기화 반영)
    @Override
    @Transactional
    public void deleteAsk(Long askId, Long memberId) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        boolean isWriter = ask.getMemberId().getMemberId().equals(memberId);
        boolean isAdmin = MemberRole.ADMIN.equalsKey(member.getMemberType());

        if (!isWriter && !isAdmin) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        List<AskImage> images = askImageRepository.findByAskId_AskId(askId);
        List<String> filesToDelete = images.stream()
                .map(AskImage::getAskImageFilename)
                .toList();

        // DB 상에서 이미지 및 문의글 엔티티 선 삭제 처리
        askImageRepository.deleteAll(images);
        askRepository.delete(ask);

        // 💡 [트랜잭션 정합성 보호] DB 삭제가 정상 커밋된 후에만 파일 디스크에서 실제 삭제 수행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String filename : filesToDelete) {
                    try {
                        fileUtil.deleteFile(filename);
                    } catch (Exception e) {
                        log.error("문의 삭제 후 커밋 파일 제거 실패: {}", filename, e);
                    }
                }
            }
        });

        if (isAdmin && !isWriter) {
            notificationManager.notifyUser(
                    ask.getMemberId(),
                    NotificationType.ASK_DELETE.getCode(),
                    ask.getAskId(),
                    memberId
            );
        }
    }

    // 6. 마이페이지 문의글 목록 조회 및 기타 메서드들...
    @Override
    public PageResponseDTO<AskListResponseDTO> getMyAskList(Long memberId, Integer askType, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("askId");

        Page<Ask> askPage = askRepository.searchMyAsks(
                memberId,
                pageRequestDTO.getSearchType(),
                pageRequestDTO.getKeyword(),
                pageRequestDTO.getSort(),
                askType,
                pageable
        );

        List<AskListResponseDTO> dtoList = askPage.getContent().stream()
                .map(AskListResponseDTO::from)
                .toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, askPage.getTotalElements());
    }

    @Override
    public PageResponseDTO<AskListResponseDTO> findByExhibitionId_ExhibitionId(Long exhibitionId, PageRequestDTO pageRequestDTO) {
        Page<Ask> result = askRepository.findByExhibitionId_ExhibitionId(exhibitionId, pageRequestDTO.getPageable("askCreatedAt"));
        List<AskListResponseDTO> dtoList = result.getContent().stream()
                .map(AskListResponseDTO::from)
                .toList();

        return new PageResponseDTO<>(
                dtoList,
                pageRequestDTO,
                result.getTotalElements()
        );
    }

    @Override
    public PageResponseDTO<AskListResponseDTO> getAsksWithoutReplies(
            PageRequestDTO pageRequestDTO
    ) {
        Pageable pageable = pageRequestDTO.getPageable("askCreatedAt");

        Page<Ask> result =
                askRepository.findAsksWithoutReplies(pageable);

        List<AskListResponseDTO> dtoList = result.getContent()
                .stream()
                .map(AskListResponseDTO::from)
                .toList();

        return new PageResponseDTO<>(
                dtoList,
                pageRequestDTO,
                result.getTotalElements()
        );
    }
}