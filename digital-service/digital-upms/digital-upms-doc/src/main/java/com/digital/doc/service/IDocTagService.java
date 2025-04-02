package com.digital.doc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.doc.dto.DocFileTagDto;
import com.digital.model.doc.dto.DocTagDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.entity.DocTagEntity;
import com.digital.model.doc.response.DocTagResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
public interface IDocTagService extends IService<DocTagEntity> {

	DocTagEntity saveTag(DocTagDto docTagDto);

	int deleteTag(Long tagId);

	void saveTags2Files(DocFileTagDto fileTagDto);

	List<DocTagEntity> findMyTags();

	Page<DocTagResponse> findPublicTags(DocTagDto docTagDto, Page page);

	IPage<DocFileEntity> findFilesByTag(DocTagDto docTagDto, PageDTO page);

	void deleteFilesWithTag(List<Long> fileIds);

	List<DocTagEntity> findTagByFileId(Long fileId);
}
