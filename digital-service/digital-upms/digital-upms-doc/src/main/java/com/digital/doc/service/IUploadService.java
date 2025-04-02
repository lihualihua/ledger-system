package com.digital.doc.service;

import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {
    DocFileEntity upload(MultipartFile file, DocFileDto fileDto);

    DocFileEntity uploadChunk(MultipartFile file, int chunkIndex, int totalChunks, DocFileDto fileDto);

    DocFileEntity folderUpload(MultipartFile file, int chunkIndex, int totalChunks, DocFileDto fileDto);
}
