package com.project.articket.wish.repository;

import com.project.articket.wish.entity.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {


     // 1. 특정 회원이 특정 전시를 위시리스트에 추가했는지 확인 (단건 조회)
    Optional<Wish> findByMemberId_MemberIdAndExhibitionId_ExhibitionId(Long memberId, Long exhibitionId);

    //2. 특정 회원이 특정 전시를 위시리스트에 추가했는지 여부 확인 (존재 여부)
    boolean existsByMemberId_MemberIdAndExhibitionId_ExhibitionId(Long memberId, Long exhibitionId);

    // 3. 마이페이지 - 특정 회원의 위시리스트 목록 조회 (페이징 + N+1 방지 Fetch Join)
    @Query(value = "SELECT w FROM Wish w " +
            "JOIN FETCH w.exhibitionId e " +
            "WHERE w.memberId.memberId = :memberId " +
            "ORDER BY w.wishId DESC",
            countQuery = "SELECT COUNT(w) FROM Wish w WHERE w.memberId.memberId = :memberId")
    Page<Wish> findWishListByMemberId(@Param("memberId") Long memberId, Pageable pageable);

     // 4. 특정 전시의 총 위시리스트 수 카운트 (전시 상세/목록용)
    long countByExhibitionId_ExhibitionId(Long exhibitionId);

    //5. 특정 회원의 위시 및 전시 삭제 시 일괄 삭제 (필요 시)
    void deleteByMemberId_MemberIdAndExhibitionId_ExhibitionId(Long memberId, Long exhibitionId);
}