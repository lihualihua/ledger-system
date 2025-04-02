
package com.digital.doc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.CheckedException;
import com.digital.common.security.service.PigUser;
import com.digital.doc.config.DocFileConfig;
import com.digital.doc.enums.OwnershipEnum;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.mapper.DocSearchMapper;
import com.digital.doc.service.IDocSearchService;
import com.digital.model.doc.contains.DocContains;
import com.digital.model.doc.dto.DocSearchDto;
import com.digital.model.doc.entity.DocSearchEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Slf4j
@Service
public class DocSearchServiceImpl extends ServiceImpl<DocSearchMapper, DocSearchEntity> implements IDocSearchService {
	private static final Logger LOGGER = LogManager.getLogger(DocSearchServiceImpl.class);

	@Autowired
	private DocSearchMapper docSearchMapper;

	@Autowired
	private UserHelper userHelper;

	@Autowired
	private DocFileConfig docFileConfig;

    @Override
	public IPage<DocSearchEntity> findSearchFiles(DocSearchDto searchDto, PageDTO<DocSearchEntity> page) {
		// 查询子文件
		if (Optional.ofNullable(searchDto.getParentId()).orElse(0L) != 0L) {
			return findChildFiles(searchDto, page);
		}
		return docSearchMapper.findSearchFiles(page, buildFileEntity(searchDto), userHelper.getUserId());
	}

	public IPage<DocSearchEntity> findChildFiles(DocSearchDto searchDto, PageDTO<DocSearchEntity> page) {
		// 如果fileName不为空，表示在页面根据文件名称搜索，需查询文件夹下所有文件
		if (StringUtils.isNotBlank(searchDto.getFileName())) {
			return findAllChildFiles(searchDto, page);
		}
		PigUser user = userHelper.getUser();
		DocSearchEntity SearchEntity = buildFileEntity(searchDto);
		boolean parentIdFlag = true;
		// 如果是文档或者图片，不根据parentId过滤
		if (StringUtils.equalsAny(searchDto.getCategory(), DocContains.CATEGORY_DOCUMENT, DocContains.CATEGORY_PICTURE)) {
			SearchEntity.setParentId(null);
			parentIdFlag = false;
		}
		if (OwnershipEnum.USER.getCode() == searchDto.getOwnership()) {
			return docSearchMapper.findChildFiles(page, SearchEntity, user.getId(), null);
		} else if (OwnershipEnum.DEPT.getCode() == searchDto.getOwnership()) {
			return docSearchMapper.findChildFiles(page, SearchEntity, null, getDeptId(searchDto.getDeptId()));
		}
		return findCommonFiles(searchDto, page, parentIdFlag);
	}

	public IPage<DocSearchEntity> findAllChildFiles(DocSearchDto searchDto, PageDTO<DocSearchEntity> page) {
		DocSearchEntity SearchEntity = buildFileEntity(searchDto);
		if (Objects.isNull(SearchEntity.getParentId())) {
			SearchEntity.setParentId(DocContains.ROOT_PARENT_ID);
		}
		// 公共文档不关联用户和部门
		if (SearchEntity.getOwnership() == OwnershipEnum.USER.getCode()) {
			return baseMapper.findAllChildFiles(page, SearchEntity, userHelper.getUserId(), null);
		} else if (SearchEntity.getOwnership() == OwnershipEnum.DEPT.getCode()) {
			return baseMapper.findAllChildFiles(page, SearchEntity, null, getDeptId(searchDto.getDeptId()));
		}
		return baseMapper.findAllChildFiles(page, SearchEntity, null, null);
	}

	private Long getDeptId(Long deptId) {
		if (Objects.nonNull(deptId)) {
			userHelper.checkDeptPermission(deptId);
		} else {
			deptId = userHelper.getDeptId();
		}
		return deptId;
	}

	private DocSearchEntity buildFileEntity(DocSearchDto dto) {
		DocSearchEntity fileEntity = new DocSearchEntity();
		fileEntity.setSearchType(dto.getSearchType());
		fileEntity.setParentId(dto.getParentId());
		fileEntity.setFileName(dto.getFileName());
		fileEntity.setOwnership(dto.getOwnership());
		fileEntity.setTagAndOr(dto.getTagAndOr());
		fileEntity.setTag(dto.getTag());
		fileEntity.setCreateByAndOr(dto.getCreateByAndOr());
		fileEntity.setCreateBy(dto.getCreateBy());
		fileEntity.setFileType(dto.getFileType());
		fileEntity.setFolderFlag(dto.getFolderFlag());
		fileEntity.setStartTime(dto.getStartTime());
		fileEntity.setEndTime(dto.getEndTime());
		fileEntity.setUpdateTimeSlot(dto.getUpdateTimeSlot());
		fileEntity.setUpdateTime(dto.getUpdateTime());
		fileEntity.setDelFlag(dto.getDelFlag());
		setQueryCategory(fileEntity, dto);
		return fileEntity;
	}

	private IPage<DocSearchEntity> findCommonFiles(DocSearchDto fileDto, PageDTO<DocSearchEntity> page, boolean parentIdFlag) {
		LambdaQueryWrapper<DocSearchEntity> queryWrapper = new LambdaQueryWrapper<>();
		if (parentIdFlag) {
			queryWrapper.eq(DocSearchEntity::getParentId, fileDto.getParentId());
		}
		queryWrapper.eq(DocSearchEntity::getOwnership, fileDto.getOwnership());
		queryWrapper.eq(DocSearchEntity::getDelFlag, fileDto.getDelFlag());
		setFileName(fileDto, queryWrapper);
		setQueryCategory(fileDto, queryWrapper);
		queryWrapper.orderByDesc(Objects.equals(fileDto.getDelFlag(), 1), DocSearchEntity::getFolderFlag);
		queryWrapper.orderByDesc(DocSearchEntity::getCreateTime);
		queryWrapper.orderByAsc(DocSearchEntity::getFileName);
		return baseMapper.selectPage(page, queryWrapper);
	}

	private void setQueryCategory(DocSearchEntity searchEntity, DocSearchDto dto) {
		String category = dto.getCategory();
		if (StringUtils.isBlank(category) || DocContains.CATEGORY_ALL.equals(category)) {
			searchEntity.setCategory(null);
			return;
		}
		searchEntity.setCategory(category);
		checkCateGory(category);
		switch (category) {
			case DocContains.CATEGORY_DOCUMENT:
				String fileType = dto.getFileType();
				searchEntity.setFileTypeList(getFileTypes(fileType));
				break;
			case DocContains.CATEGORY_FOLDER:
				searchEntity.setFolderFlag(1);
				break;
			case DocContains.CATEGORY_PICTURE:
				searchEntity.setFileTypeList(docFileConfig.getPictureFileTypes());
				break;
			default:
				break;
		}
	}

	private void setFileName(DocSearchDto fileDto, LambdaQueryWrapper<DocSearchEntity> queryWrapper) {
		String fileName = fileDto.getFileName();
		if (StringUtils.isBlank(fileName)) {
			return;
		}
		queryWrapper.like(DocSearchEntity::getFileName, fileName);
	}

	private void setQueryCategory(DocSearchDto fileDto, LambdaQueryWrapper<DocSearchEntity> queryWrapper) {
		String category = fileDto.getCategory();
		if (StringUtils.isBlank(category) || DocContains.CATEGORY_ALL.equals(category)) {
			return;
		}
		checkCateGory(category);
		switch (category) {
			case DocContains.CATEGORY_DOCUMENT:
				String fileType = fileDto.getFileType();
				queryWrapper.in(DocSearchEntity::getFileType, getFileTypes(fileType));
				break;
			case DocContains.CATEGORY_FOLDER:
				queryWrapper.eq(DocSearchEntity::getFolderFlag,1);
				break;
			case DocContains.CATEGORY_PICTURE:
				queryWrapper.in(DocSearchEntity::getFileType, docFileConfig.getPictureFileTypes());
				break;
			default:
				break;
		}
	}

	private void checkCateGory(String category) {
		if (!StringUtils.equalsAny(category, DocContains.CATEGORY_DOCUMENT, DocContains.CATEGORY_FOLDER,
				DocContains.CATEGORY_PICTURE)) {
			throw new CheckedException("文档分类不正确!");
		}
	}

	private List<String> getFileTypes(String fileType) {
		List<String> fileTypeList = new ArrayList<>();
		if (StringUtils.isBlank(fileType) || StringUtils.equals(fileType, DocContains.CATEGORY_DOCUMENT_ALL)) {
			fileTypeList.addAll(docFileConfig.getCategoryDocFileTypes());
			return fileTypeList;
		}
		if (!StringUtils.equalsAny(fileType, DocContains.CATEGORY_DOCUMENT_DOC, DocContains.CATEGORY_DOCUMENT_XLS,
				DocContains.CATEGORY_DOCUMENT_PDF, DocContains.CATEGORY_DOCUMENT_PPT)) {
			throw new CheckedException("文件类型不正确!");
		}

		switch (fileType) {
			case DocContains.CATEGORY_DOCUMENT_DOC:
				fileTypeList.addAll(docFileConfig.getDocFileTypes());
				break;
			case DocContains.CATEGORY_DOCUMENT_XLS:
				fileTypeList.addAll(docFileConfig.getExcelFileTypes());
				break;
			case DocContains.CATEGORY_DOCUMENT_PDF:
				fileTypeList.addAll(docFileConfig.getPdfFileTypes());
				break;
			case DocContains.CATEGORY_DOCUMENT_PPT:
				fileTypeList.addAll(docFileConfig.getPptFileTypes());
				break;
			default:
				break;
		}
		return fileTypeList;
	}

}
