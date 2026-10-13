package com.project.articket.statistic.service;


import com.project.articket.reservation.entity.ReservationCancelReason;
import com.project.articket.statistic.dto.CancelDetailResponseDTO;
import com.project.articket.statistic.dto.CancelReasonStatsResponseDTO;
import com.project.articket.statistic.dto.StatisticResponseDTO;
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

    private <T> List<T> fillDate(
            List<Object[]> result,
            LocalDate startDate,
            LocalDate endDate,
            BiFunction<LocalDate, Long, T> dtoCreator
    ) {
        Map<LocalDate, Long> dataMap = result.stream()
                .collect(Collectors.toMap(
                        row -> ((LocalDateTime) row[0]).toLocalDate(),
                        row -> ((Number) row[1]).longValue()
                ));

        List<T> resultList = new ArrayList<>();

        for (LocalDate date = startDate;
             !date.isAfter(endDate);
             date = date.plusDays(1)) {

            Long value = dataMap.getOrDefault(date, 0L);
            resultList.add(dtoCreator.apply(date, value));
        }

        return resultList;
    }

    public List<StatisticResponseDTO> findProfitList(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Object[]> result =
                repository.findProfit(exhibitionId, startDate, endDate);

        return fillDate(
                result,
                startDate,
                endDate,
                StatisticResponseDTO::new
        );
    }

    public Long findTotalProfit(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return findProfitList(exhibitionId, startDate, endDate)
                .stream()
                .mapToLong(StatisticResponseDTO::getValue)
                .sum();
    }

    public List<StatisticResponseDTO> findReservationList(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Object[]> result =
                repository.findReservation(exhibitionId, startDate, endDate);

        return fillDate(
                result,
                startDate,
                endDate,
                StatisticResponseDTO::new
        );
    }

    public Long findTotalReservation(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return findReservationList(exhibitionId, startDate, endDate)
                .stream()
                .mapToLong(StatisticResponseDTO::getValue)
                .sum();
    }


    public List<StatisticResponseDTO> findCanceledReservationList(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Object[]> result =
                repository.findCanceledReservation(exhibitionId, startDate, endDate);

        return fillDate(
                result,
                startDate,
                endDate,
                StatisticResponseDTO::new
        );
    }

    public Long findTotalCanceledReservation(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return findCanceledReservationList(exhibitionId, startDate, endDate)
                .stream()
                .mapToLong(StatisticResponseDTO::getValue)
                .sum();
    }

    public List<StatisticResponseDTO> findVisitorList(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Object[]> result =
                repository.findVisitor(exhibitionId, startDate, endDate);

        return fillDate(
                result,
                startDate,
                endDate,
                StatisticResponseDTO::new
        );
    }

    public Long findTotalVisitor(
            Long exhibitionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return findVisitorList(exhibitionId, startDate, endDate)
                .stream()
                .mapToLong(StatisticResponseDTO::getValue)
                .sum();
    }

    public List<CancelReasonStatsResponseDTO> findCancelReasonStats(Long exhibitionId){
        List<Object[]> result = repository.findCancelReasonStats(exhibitionId);
        return result.stream()
                .map(row->new CancelReasonStatsResponseDTO(
                        ReservationCancelReason.valueOf((String) row[0]),
                        ((Number) row[1]).longValue()
                ))
        .toList();
    }

    public List<CancelDetailResponseDTO> findCancelDetail(Long exhibitionId){
        List<Object[]> result = repository.findCancelDetail(exhibitionId);
        return result.stream()
                .map(row-> new CancelDetailResponseDTO(
                        ((LocalDateTime) row[0]),
                        ((String) row[1])
                ))
        .toList();
    }

}