package com.digital.doc.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.constant.CacheConstants;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.config.DocFileConfig;
import com.digital.doc.enums.OwnershipEnum;
import com.digital.doc.helper.DictHelper;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.mapper.DocFileMapper;
import com.digital.doc.mapper.DocFileTagMapper;
import com.digital.doc.service.IDocQueryService;
import com.digital.model.doc.contains.DocContains;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.vo.DocFileCountVO;
import com.digital.model.doc.vo.DocFileDetailVO;
import com.digital.model.doc.vo.DocFileSizeSumVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Slf4j
@Service
public class DocQueryServiceImpl extends ServiceImpl<DocFileMapper, DocFileEntity> implements IDocQueryService {
	private static final Logger LOGGER = LogManager.getLogger(DocQueryServiceImpl.class);

	@Autowired
	private DocFileConfig docFileConfig;

	@Autowired
	private UserHelper userHelper;

	@Autowired
	private IDocQueryService self;

	@Autowired
	private DocFileTagMapper fileTagMapper;

    @Autowired
    private DictHelper dictHelper;

	@Override
	@Cacheable(value = CacheConstants.FILE_DETAILS, key = "#fileId", unless = "#result == null")
	public DocFileEntity findById(Long fileId) {
		DocFileEntity queryEntity = new DocFileEntity();
		queryEntity.setFileId(fileId);
		return findById(queryEntity);
	}

	private DocFileEntity findById(DocFileEntity queryEntity) {
		LambdaQueryWrapper<DocFileEntity> queryWrapper = new LambdaQueryWrapper<>();
		Long fileId = queryEntity.getFileId();
		Integer delFlag = queryEntity.getDelFlag();
		queryWrapper.eq(Objects.nonNull(fileId), DocFileEntity::getFileId, fileId);
		queryWrapper.eq(Objects.nonNull(delFlag), DocFileEntity::getDelFlag, delFlag);
		DocFileEntity fileEntity = baseMapper.selectOne(queryWrapper);
		if (Objects.isNull(fileEntity)) {
			LOGGER.warn("没有根据文件ID查找到数据，fileId: {}", fileId);
			return null;
		}
		// 匿名登录或超级管理员不做处理
		if (OwnershipEnum.COMMON.getCode() == fileEntity.getOwnership() || userHelper.isSuperAdmin()) {
			return fileEntity;
		}
		return baseMapper.findFileById(queryEntity, userHelper.getUserId(), userHelper.getDeptId());
	}

	@Override
	public DocFileEntity findDelById(Long fileId, boolean containDel) {
		DocFileEntity queryEntity = new DocFileEntity();
		queryEntity.setFileId(fileId);
		if (!containDel) {
			queryEntity.setDelFlag(1);
		}
		return findById(queryEntity);
	}

	@Override
	public DocFileEntity findByIdNoUser(Long fileId) {
		LambdaQueryWrapper<DocFileEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DocFileEntity::getFileId, fileId);
		queryWrapper.eq(DocFileEntity::getDelFlag, 1);
		return baseMapper.selectOne(queryWrapper);
	}

	@Override
	public List<DocFileEntity> findByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		return baseMapper.selectList(Wrappers.<DocFileEntity>lambdaQuery().in(
				DocFileEntity::getFileId, ids));
	}

	@Override
	public DocFileEntity findDocFileEntity(DocFileDto fileDto) {
		DocFileEntity entity = buildFileEntity(fileDto);
		entity.setFileName(fileDto.getFileName());
		if (OwnershipEnum.USER.getCode() == entity.getOwnership()) {
			return baseMapper.findDocFileEntityByUserId(entity, userHelper.getUserId());
		} else if (OwnershipEnum.DEPT.getCode() == entity.getOwnership()) {
			Long deptId = getDeptId(fileDto.getDeptId());
			return baseMapper.findDocFileEntityByDeptId(entity, deptId);
		}
		LambdaQueryWrapper<DocFileEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DocFileEntity::getFolderFlag,entity.getFolderFlag());
		queryWrapper.eq(DocFileEntity::getDelFlag,entity.getDelFlag());
		queryWrapper.eq(DocFileEntity::getParentId,entity.getParentId());
		queryWrapper.eq(DocFileEntity::getFileName,entity.getFileName());
		queryWrapper.eq(DocFileEntity::getOwnership,entity.getOwnership());
		return baseMapper.selectOne(queryWrapper);
	}

	@Override
	public IPage<DocFileEntity> findChildFiles(DocFileDto fileDto, PageDTO<DocFileEntity> page) {
		// 如果fileName不为空，表示在页面根据文件名称搜索，需查询文件夹下所有文件
		if (StringUtils.isNotBlank(fileDto.getFileName())) {
			return findAllChildFiles(fileDto, page);
		}
		DocFileEntity fileEntity = setQueryCondition(fileDto);
		//		return findCommonFiles(fileDto, page, parentIdFlag);
		return baseMapper.findChildFiles(page, fileEntity,
				getUserIdByOwnership(fileEntity.getOwnership()),
				getDeptIdByOwnership(fileEntity.getOwnership(), fileDto.getDeptId()));

	}

	@Override
	public IPage<DocFileEntity> findAllChildFiles(DocFileDto docFileDto, PageDTO<DocFileEntity> page) {
		DocFileEntity fileEntity = setQueryCondition(docFileDto);
		return baseMapper.findAllChildFiles(page, fileEntity,
				getUserIdByOwnership(fileEntity.getOwnership()),
				getDeptIdByOwnership(fileEntity.getOwnership(), docFileDto.getDeptId()));
	}

	private DocFileEntity setQueryCondition(DocFileDto fileDto) {
		DocFileEntity fileEntity = buildFileEntity(fileDto);
		// 如果是文档或者图片，不根据parentId过滤
		if (StringUtils.equalsAny(fileDto.getCategory(), DocContains.CATEGORY_DOCUMENT, DocContains.CATEGORY_PICTURE)) {
			if (StringUtils.equalsAny(fileDto.getFileType(), DocContains.CATEGORY_DOCUMENT_OTHER)) {
				fileEntity.setFileType(DocContains.CATEGORY_DOCUMENT_OTHER);
			}
			fileEntity.setParentId(null);
		}
		return fileEntity;
	}

	private Long getUserIdByOwnership(Integer ownership) {
		if (ownership == OwnershipEnum.USER.getCode()) {
			return userHelper.getUserId();
		}
		return null;
	}

	private Long getDeptIdByOwnership(Integer ownership, Long checkedDeptId) {
		if (ownership == OwnershipEnum.DEPT.getCode()) {
			return getDeptId(checkedDeptId);
		}
		return null;
	}

	private Long getDeptId(Long deptId) {
		if (Objects.nonNull(deptId)) {
			userHelper.checkDeptPermission(deptId);
		} else {
			deptId = userHelper.getDeptId();
		}
		return deptId;
	}

	private IPage<DocFileEntity> findCommonFiles(DocFileDto fileDto, PageDTO<DocFileEntity> page, boolean parentIdFlag) {
		LambdaQueryWrapper<DocFileEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(parentIdFlag, DocFileEntity::getParentId, fileDto.getParentId());
		queryWrapper.eq(DocFileEntity::getOwnership, fileDto.getOwnership());
		queryWrapper.eq(DocFileEntity::getDelFlag, fileDto.getDelFlag());
		setFileName(fileDto, queryWrapper);
		setQueryCategory(fileDto, queryWrapper);
		queryWrapper.orderByDesc(Objects.equals(fileDto.getDelFlag(), 1), DocFileEntity::getFolderFlag);
		queryWrapper.orderByDesc(DocFileEntity::getCreateTime);
		queryWrapper.orderByAsc(DocFileEntity::getFileName);
		return baseMapper.selectPage(page, queryWrapper);
	}

	@Override
	public IPage<DocFileEntity> findCollectFiles(DocFileDto docFileDto, PageDTO<DocFileEntity> page) {
		return baseMapper.findCollectFiles(page, buildFileEntity(docFileDto), userHelper.getUserId());
	}

	@Override
	public List<DocFileEntity> findAllChildFiles(List<Long> parentIds, boolean containSelf, boolean containDel, boolean diffUser) {
		List<DocFileEntity> fileEntityList = new ArrayList<>();
		List<DocFileEntity> fileResult = new ArrayList<>();
		parentIds.forEach(item -> {
			DocFileEntity fileEntity = null;
			if (diffUser) {
				fileEntity = self.findById(item);
			} else {
				fileEntity = self.findByIdNoUser(item);
			}

			if (Objects.isNull(fileEntity)) {
				return;
			}
			if (containDel) {
				fileResult.add(fileEntity);
			} else {
				if (fileEntity.getDelFlag() == 1) {
					fileResult.add(fileEntity);
				}
			}
		});
		if (containSelf) {
			fileEntityList.addAll(fileResult);
		}
		fileEntityList.addAll(findChildFileEntityList(fileResult, containDel, diffUser));
		return fileEntityList;
	}

	@Override
	public List<DocFileEntity> findChildFilesByParentId(Long parentId) {
		return baseMapper.selectList(findChildFilesQueryWrapper(parentId));
	}

	@Override
	public IPage<DocFileEntity> findChildFilesPageByParentId(Long parentId, PageDTO page) {
		return baseMapper.selectPage(page, findChildFilesQueryWrapper(parentId));
	}

	private LambdaQueryWrapper<DocFileEntity> findChildFilesQueryWrapper(Long parentId) {
		LambdaQueryWrapper<DocFileEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DocFileEntity::getParentId, parentId);
		queryWrapper.eq(DocFileEntity::getDelFlag, 1);
		queryWrapper.orderByDesc(DocFileEntity::getFolderFlag);
		queryWrapper.orderByDesc(DocFileEntity::getCreateTime);
		queryWrapper.orderByAsc(DocFileEntity::getFileName);
		return queryWrapper;
	}

	@Override
	public DocFileSizeSumVO findFileSizeSum(List<DocFileDto> docFileDto) {
		List<Long> fileIds = docFileDto.stream().map(DocFileDto::getFileId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(fileIds)) {
			throw new CheckedException("未选中文件或文件夹!");
		}
		List<DocFileEntity> entityList = findAllChildFiles(fileIds, true, false, false);
		if (CollectionUtils.isEmpty(entityList)) {
			throw new CheckedException("文件或文件夹不存在!");
		}
		entityList = entityList.stream().filter(item -> item.getFolderFlag() == 0).collect(Collectors.toList());
		DocFileSizeSumVO fileSizeSumVO = new DocFileSizeSumVO();
		if (CollectionUtils.isEmpty(entityList)) {
			fileSizeSumVO.setFileSizeSum(0L);
			fileSizeSumVO.setFileSizeSumStr("0 KB");
		} else {
			long sum = entityList.stream().mapToLong(DocFileEntity::getFileSize).sum();
			fileSizeSumVO.setFileSizeSum(sum);
			fileSizeSumVO.setFileSizeSumStr(FileUtil.readableFileSize(sum));
		}
		return fileSizeSumVO;
	}

	@Override
	public DocFileCountVO findFileCount() {
		DocFileEntity fileEntity = new DocFileEntity();
		fileEntity.setFolderFlag(0);
		fileEntity.setDelFlag(1);
		fileEntity.setOwnership(OwnershipEnum.COMMON.getCode());
		Long commonFileCount = baseMapper.selectCount(Wrappers.<DocFileEntity>lambdaQuery().eq(
				DocFileEntity::getOwnership, fileEntity.getOwnership()).eq(
						DocFileEntity::getDelFlag,fileEntity.getDelFlag()).eq(
								DocFileEntity::getFolderFlag, fileEntity.getFolderFlag()));
		DocFileCountVO fileCountVO = new DocFileCountVO();
		fileCountVO.setCommonFileCount(commonFileCount);
		fileEntity.setOwnership(OwnershipEnum.USER.getCode());
		fileCountVO.setUserFileCount(baseMapper.findUserDeptFileCount(fileEntity, userHelper.getUserId(), null));
		fileEntity.setOwnership(OwnershipEnum.DEPT.getCode());
		fileCountVO.setDeptFileCount(baseMapper.findUserDeptFileCount(fileEntity, null, userHelper.getDeptId()));
		return fileCountVO;
	}

	@Override
	public DocFileDetailVO findFileDetailInfo(Long fileId, boolean containSelf) {
		DocFileEntity fileEntity = self.findById(fileId);
		if (Objects.isNull(fileEntity)) {
			return null;
		}
		DocFileDetailVO fileDetailVO = new DocFileDetailVO();
		fileDetailVO.setFileName(fileEntity.getFileName());
		fileDetailVO.setCreateTime(fileEntity.getCreateTime());
		fileDetailVO.setUpdateTime(fileEntity.getUpdateTime());
		fileDetailVO.setIcon(fileEntity.getIcon());
		fileDetailVO.setFileType(fileEntity.getFileType());
		fileDetailVO.setFileSizeSum(fileEntity.getFileSize());
		fileDetailVO.setFileSizeSumStr(fileEntity.getFileSizeStr());
		fileDetailVO.setFolderFlag(fileEntity.getFolderFlag());
		setFolderAttr(fileEntity, fileDetailVO);
		setLocation(fileEntity, fileDetailVO, containSelf);
		fileDetailVO.setTags(fileTagMapper.findTagByFileId(fileId, userHelper.getUserId()));
		// 仅文件返回下载地址，文件夹不做处理
		if (Objects.equals(fileEntity.getFolderFlag(), 0)) {
			String downloadUrl = dictHelper.getStringValue("downloadUrl", "downloadUrl") + fileEntity.getFileId();
			fileDetailVO.setDownloadUrl(downloadUrl);
		}
		return fileDetailVO;
	}

	private void setFolderAttr(DocFileEntity fileEntity, DocFileDetailVO fileDetailVO) {
		// 不是文件夹，则不做处理
		if (fileEntity.getFolderFlag() != 1) {
			return;
		}
		List<Long> fileIdList = new ArrayList<>();
		fileIdList.add(fileEntity.getFileId());
		List<DocFileEntity> entityList = findAllChildFiles(fileIdList, false, false, false);
		if (CollectionUtils.isEmpty(entityList)) {
			fileDetailVO.setFileCount(0);
			fileDetailVO.setFolderCount(0);
			fileDetailVO.setFileSizeSum(0L);
			fileDetailVO.setFileSizeSumStr("0 KB");
		} else {
			List<DocFileEntity> fileList = entityList.stream().filter(item -> item.getFolderFlag() == 0).collect(Collectors.toList());
			List<DocFileEntity> folderList = entityList.stream().filter(item -> item.getFolderFlag() == 1).collect(Collectors.toList());
			fileDetailVO.setFileCount(fileList.size());
			fileDetailVO.setFolderCount(folderList.size());
			long sum = fileList.stream().mapToLong(DocFileEntity::getFileSize).sum();
			fileDetailVO.setFileSizeSum(sum);
			fileDetailVO.setFileSizeSumStr(FileUtil.readableFileSize(sum));
		}
	}

	private void setLocation(DocFileEntity fileEntity, DocFileDetailVO fileDetailVO, boolean containSelf) {
		// 查找根路径
		String location = OwnershipEnum.getRootPathByValue(fileEntity.getOwnership());
		if (StringUtils.isBlank(location)) {
			throw new ApplicationException("服务端数据异常，请联系管理员!");
		}
		Long parentId = fileEntity.getParentId();
		if (Objects.equals(parentId, 0L)) {
			fileDetailVO.setLocation(location);
			return;
		}
		List<DocFileEntity> allParentFiles = baseMapper.findAllParentFiles(fileEntity);
		Map<Long, List<DocFileEntity>> helpMap = allParentFiles.stream().collect(Collectors.groupingBy(DocFileEntity::getParentId));
		StringBuilder builder = new StringBuilder(location);
		// 从根节点开始拼接路径
		String parentLocation = getParentFileName(helpMap, 0L, builder);
		if (Objects.equals(fileEntity.getFolderFlag(), 1) && containSelf) {
			parentLocation = parentLocation + "/" + fileEntity.getFileName();
		}
		fileDetailVO.setLocation(parentLocation);
	}

	private String getParentFileName(Map<Long, List<DocFileEntity>> helpMap, Long fileId, StringBuilder builder) {
		List<DocFileEntity> fileEntityList = helpMap.get(fileId);
		if (CollectionUtils.isEmpty(fileEntityList)) {
			return builder.toString();
		}
		DocFileEntity childFileEntity = helpMap.get(fileId).get(0);
		if (Objects.equals(childFileEntity.getFolderFlag(), 0)) {
			return builder.toString();
		}
		builder.append("/");
		builder.append(childFileEntity.getFileName());
		return getParentFileName(helpMap, childFileEntity.getFileId(), builder);
	}

	private List<DocFileEntity> findChildFileEntityList(List<DocFileEntity> fileResult, boolean containDel, boolean diffUser) {
		// 获取文件夹下的子文件
		List<DocFileEntity> childFileEntityList = new ArrayList<>();
		// 过滤回收的文件
		if (!containDel) {
			fileResult = fileResult.stream().filter(item -> item.getDelFlag() == 1).collect(Collectors.toList());
		}
		if (CollectionUtils.isEmpty(fileResult)) {
			return childFileEntityList;
		}
		// 过滤文件夹
		List<Long> folderIds = fileResult.stream().filter(item -> item.getFolderFlag() == 1).map(DocFileEntity::getFileId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(folderIds)) {
			return childFileEntityList;
		}
		List<DocFileEntity> childResult = findFileListByParentIds(folderIds, containDel, diffUser);
		if (CollectionUtils.isNotEmpty(childResult)) {
			childFileEntityList.addAll(childResult);
			childFileEntityList.addAll(findChildFileEntityList(childResult, containDel, diffUser));
		}
		return childFileEntityList;
	}

	private List<DocFileEntity> findFileListByParentIds(List<Long> folderIds, boolean containDel, boolean diffUser) {
		if (diffUser) {
			return baseMapper.findFileByParentIds(folderIds, userHelper.getUserId(), userHelper.getDeptId(), containDel);
		}
		LambdaQueryWrapper<DocFileEntity> queryWrapper = new LambdaQueryWrapper<>();
		if (!containDel) {
			queryWrapper.eq(DocFileEntity::getDelFlag, 1);
		}
		queryWrapper.in(DocFileEntity::getParentId, folderIds);
		return baseMapper.selectList(queryWrapper);
	}

	private DocFileEntity buildFileEntity(DocFileDto dto) {
		DocFileEntity fileEntity = new DocFileEntity();
		fileEntity.setFolderFlag(dto.getFolderFlag());
		fileEntity.setDelFlag(dto.getDelFlag());
		fileEntity.setParentId(dto.getParentId());
		fileEntity.setFileName(dto.getFileName());
		fileEntity.setFileType(dto.getFileType());
		fileEntity.setOwnership(dto.getOwnership());
		setQueryCategory(fileEntity, dto);
		return fileEntity;
	}

	private void setQueryCategory(DocFileEntity fileEntity, DocFileDto dto) {
		String category = dto.getCategory();
		if (StringUtils.isBlank(category) || DocContains.CATEGORY_ALL.equals(category)) {
			fileEntity.setCategory(null);
			return;
		}
		fileEntity.setCategory(category);
		checkCateGory(category);
		switch (category) {
			case DocContains.CATEGORY_DOCUMENT:
				String fileType = dto.getFileType();
				fileEntity.setFileTypeList(getFileTypes(fileType));
				break;
			case DocContains.CATEGORY_FOLDER:
				fileEntity.setFolderFlag(1);
				break;
			case DocContains.CATEGORY_PICTURE:
				fileEntity.setFileTypeList(docFileConfig.getPictureFileTypes());
				break;
			default:
				break;
		}
	}

	private void setFileName(DocFileDto fileDto, LambdaQueryWrapper<DocFileEntity> queryWrapper) {
		String fileName = fileDto.getFileName();
		if (StringUtils.isBlank(fileName)) {
			return;
		}
		queryWrapper.like(DocFileEntity::getFileName, fileName);
	}

	private void checkCateGory(String category) {
		if (!StringUtils.equalsAny(category, DocContains.CATEGORY_DOCUMENT, DocContains.CATEGORY_FOLDER,
				DocContains.CATEGORY_PICTURE)) {
			throw new CheckedException("文档分类不正确!");
		}
	}

	private void setQueryCategory(DocFileDto fileDto, LambdaQueryWrapper<DocFileEntity> queryWrapper) {
		String category = fileDto.getCategory();
		if (StringUtils.isBlank(category) || DocContains.CATEGORY_ALL.equals(category)) {
			return;
		}
		checkCateGory(category);
		switch (category) {
			case DocContains.CATEGORY_DOCUMENT:
				String fileType = fileDto.getFileType();
				if (DocContains.CATEGORY_DOCUMENT_OTHER.equals(fileType)) {
					// 其他类型 不查文件夹
					queryWrapper.eq(DocFileEntity::getFolderFlag,0);
					queryWrapper.notIn(DocFileEntity::getFileType, getFileTypes(fileType));
				} else {
					queryWrapper.in(DocFileEntity::getFileType, getFileTypes(fileType));
				}
				break;
			case DocContains.CATEGORY_FOLDER:
				queryWrapper.eq(DocFileEntity::getFolderFlag,1);
				break;
			case DocContains.CATEGORY_PICTURE:
				queryWrapper.in(DocFileEntity::getFileType, docFileConfig.getPictureFileTypes());
				break;
			default:
				break;
		}
	}

	private List<String> getFileTypes(String fileType) {
		List<String> fileTypeList = new ArrayList<>();
		if (StringUtils.isBlank(fileType) || StringUtils.equals(fileType, DocContains.CATEGORY_DOCUMENT_ALL)) {
			fileTypeList.addAll(docFileConfig.getCategoryDocFileTypes());
			return fileTypeList;
		}
		if (!StringUtils.equalsAny(fileType, DocContains.CATEGORY_DOCUMENT_DOC, DocContains.CATEGORY_DOCUMENT_XLS,
				DocContains.CATEGORY_DOCUMENT_PDF, DocContains.CATEGORY_DOCUMENT_PPT, DocContains.CATEGORY_DOCUMENT_OTHER)) {
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
			case DocContains.CATEGORY_DOCUMENT_OTHER:
				fileTypeList.addAll(docFileConfig.getCategoryTypes());
				break;
			default:
				break;
		}
		return fileTypeList;
	}
}
