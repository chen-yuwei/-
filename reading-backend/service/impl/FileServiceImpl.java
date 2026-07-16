package com.example.reading.service.impl;

import com.example.reading.service.FileService;
import com.example.reading.util.FileUtil;
import com.example.reading.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    @Override
    public FileUploadVO upload(MultipartFile file, String subDir) {
        FileUtil.validateImage(file);
        String filename = FileUtil.generateFilename(file.getOriginalFilename());
        try {
            Path dirPath = Paths.get(uploadDir, subDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(filename);
            file.transferTo(filePath.toFile());

            FileUploadVO vo = new FileUploadVO();
            vo.setFilename(filename);
            vo.setUrl(accessUrl + "/" + subDir + "/" + filename);
            return vo;
        } catch (IOException e) {
            throw new com.example.reading.exception.BusinessException("文件上传失败");
        }
    }
}
