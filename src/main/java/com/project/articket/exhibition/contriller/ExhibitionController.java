package com.project.articket.exhibition.contriller;


import com.project.articket.exhibition.dto.ExhibitionDetailResponseDTO;
import com.project.articket.exhibition.dto.ExhibitionListItemDTO;
import com.project.articket.exhibition.dto.ExhibitionUpdateRequest;
import com.project.articket.exhibition.service.ExhibitionCommandService;
import com.project.articket.exhibition.service.ExhibitionQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/exibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionQueryService exhibitionQueryService;
    private final ExhibitionCommandService exhibitionCommandService;

    @GetMapping
    public Page<ExhibitionListItemDTO> getExhibitions (
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "true") boolean free,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size ) {
        return exhibitionQueryService.searchExhibitions(keyword, sort, free, page, size);
    }

    @GetMapping("/{exhibitionId}")
    public ExhibitionDetailResponseDTO getExhibitionDetail(@PathVariable Long exhibitionId) {
        return exhibitionQueryService.getExhibitionDetail(exhibitionId);
    }

    @PutMapping(value = "/{exhibitionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EXHIBITION_STAFF')")
    public ExhibitionDetailResponseDTO update(
            @PathVariable Long exhibitionId,
            @Valid @RequestPart("data")ExhibitionUpdateRequest request,
            @RequestPart(value = "image", required = false)MultipartFile image) {
        return exhibitionCommandService.update(exhibitionId, request, image);
    }

    @DeleteMapping("/{exhibitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long exhibitionId) {
        exhibitionCommandService.delate(exhibitionId);
    }
}
