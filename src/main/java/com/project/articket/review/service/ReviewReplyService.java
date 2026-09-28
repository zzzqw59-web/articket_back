package com.project.articket.review.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.*;

public interface ReviewReplyService {
    PageResponseDTO<ReviewReplyDTO> reviewReplyPage(Long reviewId, PageRequestDTO pageRequestDTO);

    void reviewReplyCreate(Long reviewId, ReviewReplyCreateDTO reviewReplyCreateDTO);

    void reviewReplyUpdate(Long reviewReplyId, ReviewReplyUpdateDTO reviewReplyUpdateDTO);

    void reviewReplyDelete(Long reviewReplyId);
}
