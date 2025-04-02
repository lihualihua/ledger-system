package com.digital.doc.service;

import com.digital.model.doc.entity.DocFileEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IDownloadService {
    void download(DocFileEntity docFileEntity, HttpServletResponse response);

    void segmentDownload(DocFileEntity docFileEntity, HttpServletRequest request, HttpServletResponse response );

    void downloadZip(List<Long> fileIds, HttpServletResponse response);

    Map<String, String> zip(List<Long> fileIds);
}
