package com.project.articket.statistic.controller;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.statistic.dto.StatisticResponseDTO;
import com.project.articket.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public Long findTotalProfit(@RequestParam(required = false)  Long exhibitionId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return statisticService.findTotalProfit(exhibitionId, startDate, endDate);
    }

    @GetMapping("/profit/{exhibitionId}")
    public List<StatisticResponseDTO> findProfitList(@PathVariable Long exhibitionId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return statisticService.findProfitList(exhibitionId, startDate, endDate);
    }

    @GetMapping("/visitor/total")
    public Long findTotalVisitor(@RequestParam(required = false)  Long exhibitionId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return statisticService.findTotalVisitor(exhibitionId, startDate, endDate);
    }

    @GetMapping("/visitor/{exhibitionId}")
    public List<StatisticResponseDTO> findVisitorList(@PathVariable Long exhibitionId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return statisticService.findVisitorList(exhibitionId, startDate, endDate);
    }

    @GetMapping("/reservation/total")
    public Long findTotalReservation(@RequestParam(required = false) Long exhibitionId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return statisticService.findTotalReservation(exhibitionId, startDate, endDate);
    }

    @GetMapping("/reservation/{exhibitionId}")
    public List<StatisticResponseDTO> findReservationList(@PathVariable Long exhibitionId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return statisticService.findReservationList(exhibitionId, startDate, endDate);
    }
}
