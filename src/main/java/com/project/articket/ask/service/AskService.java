package com.project.articket.ask.service;

import com.project.articket.ask.dto.AskCreateRequestDTO;
import com.project.articket.ask.dto.AskResponseDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


public interface AskService {
    Long createAsk(Long memberId, AskCreateRequestDTO requestDto, List<MultipartFile> files);
    AskResponseDTO getAskDetail(Long askId);
    void deleteAsk(Long askId, Long memberId);
}