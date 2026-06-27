package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.security.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.dir:#{systemProperties['user.dir']}/uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@AuthenticationPrincipal SecurityUser securityUser,
                                                           @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error(400, "仅支持图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ApiResponse.error(400, "文件大小不能超过5MB");
        }

        try {
            // Ensure upload dir exists
            Path uploadPath = Paths.get(uploadDir, "avatars");
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString().substring(0, 8) + ext;
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/avatars/" + filename;
            return ApiResponse.success(Map.of("url", url));
        } catch (IOException e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : ".jpg";
    }
}
