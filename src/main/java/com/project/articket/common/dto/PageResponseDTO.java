package com.project.articket.common.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.IntStream;

@ToString
@Getter
public class PageResponseDTO<E> {

    private static final int NAVIGATE_SIZE = 10;

    private final List<E> dtoList; // 실제 조회된 데이터 목록(예: Todo 목록)
    private final List<Integer> pageNumberList; // 화면에 표시할 페이지 번호 목록(예: 1,2,3,4 ...)
    private final PageRequestDTO pageRequestDTO; // 요청박은 페이지 정보. (현재 페이지 page, 페이지 갯수(크기) size 등)

    private final boolean prev; // 이전페이지 버튼 활성화 여부
    private final boolean next; // 다음페이지 버튼 활성화 여부

    private final long totalCount; // DB에 저장된 전체 데이터 갯수
    private final int prevPage; // 이전 이동시 요청할 페이지번호(없으면 0)
    private final int nextPage; // 다음 이동시 요청할 페이지번호(없으면 0)
    private final int totalPage; // 계산된 전체 페이지 수
    private final int currentPage; // 현재 페이지 번호

    // 생성자 로직
    public PageResponseDTO(List<E> dtoList, PageRequestDTO pageRequestDTO, long totalCount) {
        this.dtoList = dtoList;
        this.pageRequestDTO = pageRequestDTO;
        this.totalCount = totalCount;
        this.currentPage = pageRequestDTO.getPage();

        int size = pageRequestDTO.getSize(); // pageRequestDTO 있는 페이지갯수를 반환해서 생성함.

        // 이 계산식을 써서 전체데이터가 101개면 101나누기10은 11이니까 11페이지가됨
        this.totalPage = (int) Math.ceil((double) totalCount / size);

        // temEnd / starPage / endPage (페이지 바 범위 계산)
        // temEnd: 현재페이지 기준 10단위 끝 번호 예를들면 4페이지면 10 14페이지면 20
        // starPage:  이건 반대로 시작페이지 번호 예를들면 4페이지면 1, 14페이지면 11
        // endPage: 끝 페이지 번호 실제 데이터에 끝 번호를 나타내는 놈이라 생각하면 편함.
        int temEnd = (int) Math.ceil(currentPage / (double) NAVIGATE_SIZE) * NAVIGATE_SIZE;
        int starPage = temEnd - (NAVIGATE_SIZE - 1);
        int endPage = Math.min(temEnd, totalPage);

        // 이전 / 다음 여부
        this.prev = starPage > 1; // 시작페이지가 1보다 크면 이전 10개 블록이 존재
        this.next = endPage < totalPage; // 끝 페이지가 전체 페이지보다 작으면 다음 10개 블록이 존재

        // 삼항연산자를 통해서 페이지 이동 여부를 묻는데
        this.prevPage = prev ? starPage - 1 : 0; // 이놈은 이전페이지에 값이 있으면 이전페이지로 넘어가는데 없으면 안넘어감
        this.nextPage = next ? endPage + 1 : 0; // 반대로 눌렀을떄 값이 있으면 넘어가고 없으면 그대로.

        //페이지 번호 리스트
        // IntStream.rangeClosed(starPage, endPage)를 통해 [1,2,3,4,5,6,7,8,9,10]과 같은 숫자 배열을 만들어 반복문(map)을 돌리기 쉽다.
        this.pageNumberList = totalPage == 0
                ? List.of() : IntStream.rangeClosed(starPage, endPage).boxed().toList();

    }
}
