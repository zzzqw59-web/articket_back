package com.project.articket.common.controller;

import com.project.articket.common.util.CustomFileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class FileController {

    private final CustomFileUtil fileUtil;

    /**
     * 실물 파일(이미지/썸네일/일반 첨부파일) 스트리밍 및 다운로드
     *
     * GET /api/files/{fileName}
     * 예시:
     * - 원본 이미지: GET /api/files/UUID_xxx.jpg
     * - 썸네일 이미지: GET /api/files/s_UUID_xxx.jpg
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable("fileName") String fileName) {
        return fileUtil.getFile(fileName);
    }
}
