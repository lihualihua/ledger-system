package com.digital.doc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;

import java.util.List;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
public interface IDocFileService extends IService<DocFileEntity> {
	DocFileEntity createFile(DocFileEntity docFileEntity, Long deptId);

	DocFileDto createFolder(DocFileDto folder);

	void rename(DocFileDto folder);

	void recycle(List<DocFileDto> fileDtoList);

	void recover(List<DocFileDto> fileDtoList);

	void move(Long parentId, List<DocFileDto> fileDtoList);

	void addDownloadCount(DocFileEntity docFileEntity);

	void batchAddDownloadCount(List<DocFileEntity> docFileEntityList);

	void processCollectCount(Long fileId, boolean addFlag);

	void deleteFile(List<Long> fileIds);

	String generateFileName(DocFileDto fileDto);
}
