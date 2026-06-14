package com.school.exhibition.modules.file;

import com.school.exhibition.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /** 通用上传，按目录区分：avatar / cover / media / face */
    @PostMapping("/upload")
    public R<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String dir) {
        String url = fileService.upload(file, dir);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("name", file.getOriginalFilename());
        data.put("size", file.getSize());
        data.put("type", file.getContentType());
        return R.ok(data);
    }
}
