package com.project.articket.statistic.controller;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.statistic.dto.ProfitResponseDTO;
import com.project.articket.statistic.dto.ReservationResponseDTO;
import com.project.articket.statistic.dto.VisitorResponseDTO;
import com.project.articket.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistic")
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/exhibitionList")
    public List<Exhibition> findExhibitionByWishCount() {
        return statisticService.findExhibitionByWishCount();
    }

    @GetMapping("/profit/total")
    public Long findTotalProfit(@RequestParam(required = false)  Long exhibitionId, @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return statisticService.findTotalProfit(exhibitionId, startDate, endDate);
    }

    @GetMapping("/profit/{exhibitionId}")
    public List<ProfitResponseDTO> findProfitList(@PathVariable Long exhibitionId, @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return statisticService.findProfitList(exhibitionId, startDate, endDate);
    }

    @GetMapping("/visitor/total")
    public Long findTotalVisitor(@RequestParam(required = false)  Long exhibitionId, @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return statisticService.findTotalVisitor(exhibitionId, startDate, endDate);
    }

    @GetMapping("/visitor/{exhibitionId}")
    public List<VisitorResponseDTO> findVisitorList(@PathVariable Long exhibitionId, @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return statisticService.findVisitorList(exhibitionId, startDate, endDate);
    }

    @GetMapping("/reservation/total")
    public Long findTotalReservation(@RequestParam(required = false) Long exhibitionId, @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return statisticService.findTotalReservation(exhibitionId, startDate, endDate);
    }

    @GetMapping("/reservation/{exhibitionId}")
    public List<ReservationResponseDTO> findReservationList(@PathVariable Long exhibitionId, @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return statisticService.findReservationList(exhibitionId, startDate, endDate);
    }
}
