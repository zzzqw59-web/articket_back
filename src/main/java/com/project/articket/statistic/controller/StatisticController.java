package com.project.articket.statistic.controller;

import com.project.articket.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistic")
public class StatisticController {
    private final StatisticService statisticService;




}
