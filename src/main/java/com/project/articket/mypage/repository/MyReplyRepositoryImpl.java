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
            // 오라클 문자열 연결은 CONCAT 대신 || 또는 LIKE '%' || :keyword || '%' 사용을 권장
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

        // 2. 데이터 목록 조회 (LIMIT / OFFSET 문자열 제거!)
        String selectSql = unionSql + " ORDER BY GREATEST(combined.created_at, combined.modified_at) " + sortDirection;

        Query dataQuery = em.createNativeQuery(selectSql)
                .setParameter("memberId", memberId)
                .setFirstResult((int) pageable.getOffset()) // 👈 JPA 페이징 API 적용 (OFFSET 대체)
                .setMaxResults(pageable.getPageSize());    // 👈 JPA 페이징 API 적용 (LIMIT 대체)

        if (keyword != null && !keyword.trim().isEmpty()) {
            dataQuery.setParameter("keyword", keyword);
        }

        List<Object[]> rawList = dataQuery.getResultList();

        // Object[] -> DTO 매핑
        List<MyReplyListResponseDTO> content = rawList.stream().map(row -> MyReplyListResponseDTO.builder()
                .replyId(((Number) row[0]).longValue())
                .replyType((String) row[1])
                .targetId(((Number) row[2]).longValue())
                .replyContent((String) row[3])
                .replyCreatedAt(row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null)
                .replyModifiedAt(row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null)
                .build()).toList();

        // 3. 카운트 쿼리
        String countSql = "SELECT COUNT(*) FROM (" + unionSql + ") count_table";
        Query countQuery = em.createNativeQuery(countSql).setParameter("memberId", memberId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            countQuery.setParameter("keyword", keyword);
        }

        return PageableExecutionUtils.getPage(content, pageable, () -> ((Number) countQuery.getSingleResult()).longValue());
    }
}