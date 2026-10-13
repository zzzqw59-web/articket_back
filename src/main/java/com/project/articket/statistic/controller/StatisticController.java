package com.project.articket.statistic.controller;

import com.project.articket.statistic.dto.CancelDetailResponseDTO;
import com.project.articket.statistic.dto.CancelReasonStatsResponseDTO;
import com.project.articket.statistic.dto.StatisticRequestDTO;
import com.project.articket.statistic.dto.StatisticResponseDTO;
import com.project.articket.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistic")
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/profit/total")
    public Long findTotalProfit(@ModelAttribute StatisticRequestDTO request) {
        return statisticService.findTotalProfit(null, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/profit/{exhibitionId}")
    public List<StatisticResponseDTO> findProfitList(@PathVariable Long exhibitionId, @ModelAttribute StatisticRequestDTO request) {
        return statisticService.findProfitList(exhibitionId, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/visitor/total")
    public Long findTotalVisitor(@ModelAttribute StatisticRequestDTO request) {
        return statisticService.findTotalVisitor(null, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/visitor/{exhibitionId}")
    public List<StatisticResponseDTO> findVisitorList(@PathVariable Long exhibitionId, @ModelAttribute StatisticRequestDTO request) {
        return statisticService.findVisitorList(exhibitionId, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/reservation/total")
    public Long findTotalReservation(@ModelAttribute StatisticRequestDTO request) {
        return statisticService.findTotalReservation(null, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/reservation/{exhibitionId}")
    public List<StatisticResponseDTO> findReservationList(@PathVariable Long exhibitionId, @ModelAttribute StatisticRequestDTO request) {
        return statisticService.findReservationList(exhibitionId, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/reservation/canceled/total")
    public Long findTotalCanceledReservation(@ModelAttribute StatisticRequestDTO request) {
        return statisticService.findTotalCanceledReservation(null, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/reservation/canceled/{exhibitionId}")
    public List<StatisticResponseDTO> findCanceledReservationList(@PathVariable Long exhibitionId, @ModelAttribute StatisticRequestDTO request) {
        return statisticService.findCanceledReservationList(exhibitionId, request.getStartDate(), request.getEndDate());
    }

    @GetMapping("/cancel/total")
    public List<CancelReasonStatsResponseDTO> findTotalCancelReason(){
        return statisticService.findCancelReasonStats(null);
    }

    @GetMapping("/cancel/{exhibitionId}")
    public List<CancelReasonStatsResponseDTO> findCancelReasonList(@PathVariable Long exhibitionId){
        return statisticService.findCancelReasonStats(exhibitionId);
    }

    @GetMapping("/cancel/detail/{exhibitionId}")
    public List<CancelDetailResponseDTO> findCancelDetail(@PathVariable Long exhibitionId){
        return statisticService.findCancelDetail(exhibitionId);
    }
}
