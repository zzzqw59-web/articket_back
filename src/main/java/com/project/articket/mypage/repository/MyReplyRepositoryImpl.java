package com.project.articket.mypage.repository;

import com.project.articket.mypage.dto.MyReplyListResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MyReplyRepositoryImpl implements MyReplyRepositoryCustom {

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public Page<MyReplyListResponseDTO> searchMyReplies(Long memberId, String searchType, String keyword, String sort, Pageable pageable) {

        // 검색 조건 처리 (댓글 내용 검색 기준)
        String searchSql = "";
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchSql = " AND combined.reply_content LIKE '%' || :keyword || '%' ";
        }

        // 1. REVIEW_REPLY와 ASK_REPLY를 UNION ALL로 통합
        String unionSql =
                "SELECT * FROM (" +
                        "  SELECT review_reply_id AS reply_id, 'REVIEW' AS reply_type, review_id AS target_id, " +
                        "         review_reply_body AS reply_content, review_reply_created_at AS created_at, review_reply_modified_at AS modified_at " +
                        "  FROM REVIEW_REPLY " +
                        "  WHERE member_id = :memberId " +
                        "  UNION ALL " +
                        "  SELECT ask_reply_id AS reply_id, 'ASK' AS reply_type, ask_id AS target_id, " +
                        "         ask_reply_body AS reply_content, ask_reply_created_at AS created_at, ask_reply_modified_at AS modified_at " +
                        "  FROM ASK_REPLY " +
                        "  WHERE member_id = :memberId " +
                        ") combined " +
                        "WHERE 1=1 " + searchSql;

        // 동적 정렬 방향 결정 (기본값 DESC, pageable에 ASC가 넘어오면 ASC 적용)
        String sortDirection = "DESC";
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                if (order.isAscending()) {
                    sortDirection = "ASC";
                    break;
                }
            }
        }

        // 2. 데이터 목록 조회
        String selectSql = unionSql + " ORDER BY GREATEST(combined.created_at, combined.modified_at) " + sortDirection;

        Query dataQuery = em.createNativeQuery(selectSql)
                .setParameter("memberId", memberId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        if (keyword != null && !keyword.trim().isEmpty()) {
            dataQuery.setParameter("keyword", keyword);
        }

        List<Object[]> rawList = dataQuery.getResultList();

        // Object[] -> DTO 매핑 (Timestamp 및 LocalDateTime 안전 변환 처리)
        List<MyReplyListResponseDTO> content = rawList.stream().map(row -> {
            return MyReplyListResponseDTO.builder()
                    .replyId(((Number) row[0]).longValue())
                    .replyType((String) row[1])
                    .targetId(((Number) row[2]).longValue())
                    .replyContent((String) row[3])
                    .replyCreatedAt(convertToLocalDateTime(row[4]))
                    .replyModifiedAt(convertToLocalDateTime(row[5]))
                    .build();
        }).toList();

        // 3. 카운트 쿼리
        String countSql = "SELECT COUNT(*) FROM (" + unionSql + ") count_table";
        Query countQuery = em.createNativeQuery(countSql).setParameter("memberId", memberId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            countQuery.setParameter("keyword", keyword);
        }

        return PageableExecutionUtils.getPage(content, pageable, () -> ((Number) countQuery.getSingleResult()).longValue());
    }

    // 💡 날짜 타입(Timestamp 또는 LocalDateTime)을 안전하게 변환하는 헬퍼 메서드
    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toLocalDateTime();
        }
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        return null;
    }
}