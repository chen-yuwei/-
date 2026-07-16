package com.example.reading.service;

import com.example.reading.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadVO upload(MultipartFile file, String subDir);
}
