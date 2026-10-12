package com.project.articket.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@ToString
@Getter
@Setter
@NoArgsConstructor
public class PageRequestDTO {
    private String searchType = "";
    private String keyword = "";
    private String sort = "";

    private int page = 1;
    private int size = 10;

    public PageRequestDTO(int page, int size) {
        this.page = page;
        this.size = size;
    }
    public Pageable getPageable(String sortField) {
        // 전달받은 sort 값이 "asc"이면 ASC, 아니면 DESC 처리
        Sort.Direction direction = "asc".equalsIgnoreCase(this.sort)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                page > 0 ? page - 1 : 0, // 음수 페이지 방어
                size,
                direction,
                sortField
        );
    }
}
