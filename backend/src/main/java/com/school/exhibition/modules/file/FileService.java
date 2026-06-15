package com.school.exhibition.modules.file;

import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /** 允许上传的 MIME 类型白名单 */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            "video/mp4", "video/webm",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /** 单文件最大尺寸：50MB */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;

    public String upload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) throw new BusinessException("文件不能为空");

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件过大，最大支持 50MB");
        }

        // MIME 类型白名单
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String objectName = String.format("%s/%s/%s%s",
                dir == null ? "common" : dir,
                LocalDate.now(),
                UUID.randomUUID().toString().replace("-", ""),
                ext);
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            log.error("[MinIO] 上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + objectName;
    }

    /** 获取临时预签名URL（可选，公开桶时可省略） */
    public String presignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(1, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            throw new BusinessException("生成预览URL失败");
        }
    }
}
