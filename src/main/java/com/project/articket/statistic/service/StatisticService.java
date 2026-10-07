package com.project.articket.statistic.service;


import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.statistic.dto.ProfitResponseDTO;
import com.project.articket.statistic.dto.ReservationResponseDTO;
import com.project.articket.statistic.dto.VisitorResponseDTO;
import com.project.articket.statistic.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {
    private final StatisticRepository repository;

    private <T> List<T> fillDate(List<Object[]> result, LocalDate startDate, LocalDate endDate, BiFunction<LocalDate, Long, T> dtoCreator){
        Map<LocalDate, Long> dataMap = result.stream()
                .collect(Collectors.toMap(
                        row -> ((LocalDateTime) row[0]).toLocalDate(),
                        row -> ((Number) row[1]).longValue()
                ));

        List<T> resultList = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date=date.plusDays(1)){
            Long value = dataMap.getOrDefault(date, 0L);
            resultList.add(dtoCreator.apply(date, value));
        }

        return resultList;
    }

    public List<Exhibition> findExhibitionByWishCount() {
        return repository.findExhibitionByWishCount();
    }

    public List<ProfitResponseDTO> findProfitList(Long exhibitionId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> result = repository.findProfit(exhibitionId, startDate, endDate);
        return fillDate(result, startDate.toLocalDate(), endDate.toLocalDate(), ProfitResponseDTO::new);
    }


    public Long findTotalProfit(Long exhibitionId, LocalDateTime startDate, LocalDateTime endDate) {
        return findProfitList(exhibitionId, startDate, endDate).stream()
                .mapToLong(ProfitResponseDTO::getProfit)
                .sum();
    }

    public List<ReservationResponseDTO> findReservationList(Long exhibitionId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> result = repository.findReservation(exhibitionId, startDate, endDate);
        return fillDate(result, startDate.toLocalDate(), endDate.toLocalDate(), ReservationResponseDTO::new);
    }

    public Long findTotalReservation(Long exhibitionId, LocalDateTime startDate, LocalDateTime endDate){
        return findReservationList(exhibitionId, startDate, endDate).stream()
                .mapToLong(ReservationResponseDTO::getReservation)
                .sum();
    }

    public List<VisitorResponseDTO> findVisitorList(Long exhibitionId, LocalDateTime startDate, LocalDateTime endDate){
        List<Object[]> result = repository.findVisitor(exhibitionId, startDate, endDate);
        return fillDate(result, startDate.toLocalDate(), endDate.toLocalDate(), VisitorResponseDTO::new);
    }

    public Long findTotalVisitor(Long exhibitionId, LocalDateTime startDate, LocalDateTime endDate){
        return findVisitorList(exhibitionId, startDate, endDate).stream()
                .mapToLong(VisitorResponseDTO::getVisitor)
                .sum();
    }


}


