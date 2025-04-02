package com.digital.doc.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.constant.CacheConstants;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.common.core.util.RedisUtils;
import com.digital.common.security.service.PigUser;
import com.digital.doc.enums.OwnershipEnum;
import com.digital.doc.helper.FileHelper;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.helper.UserPermissionHelper;
import com.digital.doc.mapper.DeptFileMapper;
import com.digital.doc.mapper.DocCollectMapper;
import com.digital.doc.mapper.DocFileMapper;
import com.digital.doc.mapper.UserFileMapper;
import com.digital.doc.service.IDocFileService;
import com.digital.doc.service.IDocQueryService;
import com.digital.doc.service.IDocShareService;
import com.digital.doc.service.IDocTagService;
import com.digital.model.doc.contains.DocContains;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DeptFileEntity;
import com.digital.model.doc.entity.DocCollectEntity;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.entity.UserFileEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DocFileServiceImpl extends ServiceImpl<DocFileMapper, DocFileEntity> implements IDocFileService {
	private static final Logger LOGGER = LogManager.getLogger(DocFileServiceImpl.class);

	@Autowired
	private UserHelper userHelper;

	@Autowired
	private UserFileMapper userFileMapper;

	@Autowired
	private DeptFileMapper deptFileMapper;

	@Autowired
	private DocCollectMapper docCollectMapper;

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private UserPermissionHelper userPermissionHelper;

	@Autowired
	private IDocQueryService docQueryService;

	@Autowired
	private IDocShareService docShareService;

	@Autowired
	private IDocTagService docTagService;

	@Override
	public DocFileEntity createFile(DocFileEntity docFileEntity, Long deptId) {
		insertFile(docFileEntity, deptId);
		return docFileEntity;
	}

	private void insertUserFile(Long fileId, Long userId) {
		UserFileEntity userFileEntity = new UserFileEntity();
		userFileEntity.setFileId(fileId);
		userFileEntity.setUserId(userId);
		userFileMapper.insert(userFileEntity);
	}

	private void insertDeptFile(Long fileId, Long deptId, Long userId) {
		if (Objects.nonNull(deptId)) {
			userHelper.checkDeptPermission(deptId);
		} else {
			deptId = userHelper.getDeptId();
		}
		DeptFileEntity deptFileEntity = new DeptFileEntity();
		deptFileEntity.setFileId(fileId);
		deptFileEntity.setUserId(userId);
		deptFileEntity.setDeptId(deptId);
		deptFileMapper.insert(deptFileEntity);
	}

	@Override
	public DocFileDto createFolder(DocFileDto docFileDto) {
		insertFolder(docFileDto);
		return docFileDto;
	}

	private void insertFolder(DocFileDto docFileDto) {
		// 构建文件夹属性
		DocFileEntity fileEntity = buildFileFolder(docFileDto);
		fileEntity.setFileSize(0L);
		fileEntity.setEditFlag(userPermissionHelper.getEditFlag(docFileDto.getEditFlag(), fileEntity.getOwnership(), fileEntity.getParentId()));
		Long fileId = insertFile(fileEntity, docFileDto.getDeptId());
		docFileDto.setFileId(fileId);
		createChildFolder(docFileDto);
	}

	private Long insertFile(DocFileEntity docFileEntity, Long deptId) {
		PigUser user = userHelper.getUser();
		Long userId = user.getId();
		docFileEntity.setCreateById(userId);
		baseMapper.insert(docFileEntity);

		Long fileId = docFileEntity.getFileId();
		Integer ownership = docFileEntity.getOwnership();
		if (OwnershipEnum.USER.getCode() == ownership) {
			insertUserFile(fileId, userId);
		} else if (OwnershipEnum.DEPT.getCode() == ownership) {
			insertDeptFile(fileId, deptId, userId);
		}
		return fileId;
	}

	private void createChildFolder(DocFileDto docFileDto) {
		List<DocFileDto> child = docFileDto.getChild();
		if (CollectionUtils.isEmpty(child)) {
			return;
		}
		for (DocFileDto fileDto : child) {
			fileDto.setParentId(docFileDto.getFileId());
			fileDto.setOwnership(docFileDto.getOwnership());
			insertFolder(fileDto);
		}
	}

	@Override
	@CacheEvict(value = CacheConstants.FILE_DETAILS, key = "#docFileDto.fileId")
	public void rename(DocFileDto docFileDto) {
		LambdaUpdateWrapper<DocFileEntity> wrapper = new LambdaUpdateWrapper<>();
		wrapper.eq(DocFileEntity::getFileId, docFileDto.getFileId());
		wrapper.set(DocFileEntity::getFileName, docFileDto.getFileName());
		wrapper.set(DocFileEntity::getUpdateBy, userHelper.getUser().getName());
		wrapper.set(DocFileEntity::getUpdateTime, LocalDateTime.now());
		baseMapper.update(wrapper);
	}

	@Override
	public void recycle(List<DocFileDto> fileDtoList) {
		List<Long> fileIds = getFileIds(fileDtoList);
		userPermissionHelper.checkUserRolePermission(fileIds);
		List<DocFileEntity> entities = findByIds(fileIds);
		// 回收时，校验文件夹下面是否包含文件，如果包含不允许回收
		checkChildFile(entities);
		LambdaUpdateWrapper<DocFileEntity> wrapper = new LambdaUpdateWrapper<>();
		wrapper.in(DocFileEntity::getFileId, fileIds);
		wrapper.set(DocFileEntity::getDelFlag, 0);
		wrapper.set(DocFileEntity::getUpdateBy, userHelper.getUser().getName());
		wrapper.set(DocFileEntity::getUpdateTime, LocalDateTime.now());
		baseMapper.update(wrapper);
		entities.forEach(item -> RedisUtils.del(CacheConstants.FILE_DETAILS + "::" + item.getFileId()));
	}

	@Override
	public void recover(List<DocFileDto> fileDtoList) {
		List<Long> fileIds = getFileIds(fileDtoList);
		userPermissionHelper.checkUserRolePermission(fileIds);
		List<DocFileEntity> entities = findByIds(fileIds);
		if (CollectionUtils.isEmpty(entities)) {
			throw new ApplicationException("文件不存在，请联系管理员!");
		}
		entities.forEach(item -> {
			if (item.getDelFlag() == 1) {
				return;
			}
			// 获取文件名并修改，确保文件名唯一
			DocFileDto fileDto = new DocFileDto();
			fileDto.setOwnership(item.getOwnership());
			fileDto.setParentId(item.getParentId());
			fileDto.setFileName(item.getFileName());
			fileDto.setFolderFlag(item.getFolderFlag());
			String fileName = generateFileName(fileDto);
			Long fileId = item.getFileId();
			LambdaUpdateWrapper<DocFileEntity> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(DocFileEntity::getFileId, fileId);
			updateWrapper.set(DocFileEntity::getDelFlag, 1);
			updateWrapper.set(DocFileEntity::getFileName, fileName);
			updateWrapper.set(DocFileEntity::getUpdateBy, userHelper.getUser().getName());
			updateWrapper.set(DocFileEntity::getUpdateTime, LocalDateTime.now());
			baseMapper.update(updateWrapper);
			RedisUtils.del(CacheConstants.FILE_DETAILS + "::" + fileId);
		});
	}

	private void checkChildFile(List<DocFileEntity> entities) {
		List<Long> folderIds = entities.stream().filter(item -> item.getFolderFlag() == 1).map(DocFileEntity::getFileId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(folderIds)) {
			return;
		}
		List<DocFileEntity> childFiles = docQueryService.findAllChildFiles(folderIds, false, false, true);
		if (CollectionUtils.isEmpty(childFiles)) {
			return;
		}
		throw new CheckedException("回收的文件夹中包含文件，不允许回收!");
	}

	private List<Long> getFileIds(List<DocFileDto> fileDtoList) {
		return fileDtoList.stream().map(DocFileDto::getFileId).collect(Collectors.toList());
	}

	private List<DocFileEntity> findByIds(List<Long> fileIds) {
		List<DocFileEntity> entities = docQueryService.findByIds(fileIds);
		if (CollectionUtils.isEmpty(entities)) {
			throw new CheckedException("回收文件不存在!");
		}
		return entities;
	}

	@Override
	@CacheEvict(value = CacheConstants.FILE_DETAILS, allEntries = true)
	public void move(Long parentId, List<DocFileDto> fileDtoList) {
		List<Long> fileIds = getFileIds(fileDtoList);
		userPermissionHelper.checkUserRolePermission(fileIds);
		LambdaUpdateWrapper<DocFileEntity> wrapper = new LambdaUpdateWrapper<>();
		wrapper.in(DocFileEntity::getFileId, fileIds);
		wrapper.set(DocFileEntity::getParentId, parentId);
		wrapper.set(DocFileEntity::getUpdateBy, userHelper.getUser().getName());
		wrapper.set(DocFileEntity::getUpdateTime, LocalDateTime.now());
		baseMapper.update(wrapper);
	}

	@Override
	public void addDownloadCount(DocFileEntity docFileEntity) {
		if (docFileEntity.getFileId() == null || docFileEntity.getFileId() == 0L) {
			return;
		}
		baseMapper.addDownloadCount(docFileEntity.getFileId());
	}

	@Async
	@Override
	public void batchAddDownloadCount(List<DocFileEntity> docFileEntityList) {
		if (CollectionUtils.isEmpty(docFileEntityList)) {
			return;
		}
		List<Long> allFileIds = docFileEntityList.stream().filter(fileEntity ->
				fileEntity.getFolderFlag() == 0).map(DocFileEntity::getFileId).collect(Collectors.toList());
		baseMapper.batchAddDownloadCount(allFileIds);
	}

	@Override
	public void processCollectCount(Long fileId, boolean addFlag) {
		if (addFlag) {
			baseMapper.addCollectCount(fileId);
			return;
		}
		baseMapper.subtractCollectCount(fileId);
	}

	@Override
	public void deleteFile(List<Long> fileIds) {
		// 如果是公共或部门文档，需要管理员才能删除
		userPermissionHelper.checkUserRolePermission(fileIds);

		// 获取文件夹下所有的文件ID
		List<DocFileEntity> fileEntityList = docQueryService.findAllChildFiles(fileIds, true, true, true);
		if (CollectionUtils.isEmpty(fileEntityList)) {
			return;
		}
		LOGGER.info("The system starts to delete file records. total counts: {}", fileEntityList.size());
		List<Long> allFileIds = fileEntityList.stream().map(DocFileEntity::getFileId).collect(Collectors.toList());
		List<List<Long>> partFiles = ListUtil.partition(allFileIds, 50);
		partFiles.forEach(item -> {
			// 删除收藏，分享等数据
			docCollectMapper.delete(Wrappers.<DocCollectEntity>lambdaQuery().in(
					DocCollectEntity::getFileId, item));
			docShareService.deleteShareByFileIds(item);
			docTagService.deleteFilesWithTag(item);
			userFileMapper.delete(Wrappers.<UserFileEntity>lambdaQuery().in(UserFileEntity::getFileId, item));
			deptFileMapper.delete(Wrappers.<DeptFileEntity>lambdaQuery().in(DeptFileEntity::getFileId, item));
			baseMapper.delete(Wrappers.<DocFileEntity>lambdaQuery().in(DocFileEntity::getFileId, item));
		});
		allFileIds.forEach(item -> RedisUtils.del(CacheConstants.FILE_DETAILS + "::" + item));
		fileHelper.asyncDeleteFile(fileEntityList.stream().map(DocFileEntity::getFilePath).collect(Collectors.toList()));
		LOGGER.info("The system ends by deleting the file.");
	}

	@Override
	public String generateFileName(DocFileDto fileDto) {
		fileDto.setDelFlag(1);
		DocFileEntity entity = docQueryService.findDocFileEntity(fileDto);
		if (Objects.isNull(entity)) {
			return fileDto.getFileName();
		}
		return incrementFileName(fileDto);
	}

	/**
	 * 文件名(1) 自动递增
	 *
	 * @param fileDto
	 * @return
	 */
	private String incrementFileName(DocFileDto fileDto) {
		String fileName = fileDto.getFileName();
		// 正则表达式匹配文件名中的数字序号
		Integer folderFlag = fileDto.getFolderFlag();
		String regex = getRegex(folderFlag);
		java.util.regex.Matcher matcher = Pattern.compile(regex).matcher(fileName);
		if (matcher.matches()) {
			// 提取数字序号
			int number = Integer.parseInt(matcher.group(1));
			number++;
			// 转换回字符串
			String newNumber = " (" + number + ")";
			// 替换序号并返回新文件名
			fileName = fileName.replaceFirst("\\(([0-9]+)\\)", newNumber);
		} else {
			// 拼接（2）
			fileName = getNewFileName(folderFlag, fileName);
		}
		fileDto.setFileName(fileName);
		return generateFileName(fileDto);
	}

	private String getNewFileName(Integer folderFlag, String fileName) {
		if (Objects.nonNull(folderFlag) && folderFlag == 1) {
			return fileName + " (2)";
		}
		int fileNameLastIndexOf = fileName.lastIndexOf(".");
		return fileName.substring(0, fileNameLastIndexOf) + " (2)" + fileName.substring(fileNameLastIndexOf);
	}

	private String getRegex(Integer folderFlag) {
		if (Objects.nonNull(folderFlag) && folderFlag == 1) {
			return ".*\\(([0-9]+)\\)";
		}
		return ".*\\(([0-9]+)\\)\\..*";
	}

	private DocFileEntity buildFileEntity(DocFileDto docFileDto) {
		DocFileEntity docFileEntity = new DocFileEntity();
		docFileEntity.setFileName(docFileDto.getFileName());
		docFileEntity.setOwnership(docFileDto.getOwnership());
		docFileEntity.setDelFlag(docFileDto.getDelFlag());
		docFileEntity.setFolderFlag(docFileDto.getFolderFlag());
		docFileEntity.setParentId(docFileDto.getParentId());
		docFileEntity.setCreateById(userHelper.getUserId());
		docFileEntity.setIcon(docFileDto.getIcon());
		return docFileEntity;
	}

	private DocFileEntity buildFileFolder(DocFileDto docFileDto) {
		DocFileEntity docFileEntity = buildFileEntity(docFileDto);
		docFileEntity.setDelFlag(1);
		docFileEntity.setFolderFlag(DocContains.FOLDER_FLAG);
		return docFileEntity;
	}

}
