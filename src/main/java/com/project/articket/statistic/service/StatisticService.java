package com.project.articket.statistic.service;


import com.project.articket.statistic.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticService {
    private final StatisticRepository repository;


}
