package com.digital.doc.controller;

import com.digital.common.core.util.R;
import com.digital.doc.service.IDocCollectService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@RestController
@RequestMapping("/doc/file")
@Tag(description = "doc", name = "文档管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DocFileController extends BaseController {

	@Autowired
	private IDocCollectService docCollectService;

	/**
	 * 创建文件夹
	 *
	 * @param docFileDto
	 * @return
	 */
	@PostMapping("/createFolders")
	public R createFolders(@Valid @RequestBody DocFileDto docFileDto) {
		checkDocFolder(docFileDto.getParentId());
		checkFolderExist(docFileDto);
		Integer ownership = docFileDto.getOwnership();
		checkOwnership(ownership);
		checkUserRolePermission(docFileDto.getEditFlag(), ownership);
		return R.ok(docFileService.createFolder(docFileDto));
	}

	/**
	 * 文件/文件夹重命名
	 *
	 * @param docFileDto
	 * @return
	 */
	@PostMapping("/rename")
	public R rename(@RequestBody DocFileDto docFileDto) {
		if (StringUtils.isBlank(docFileDto.getFileName())) {
			return R.failed("名称不能为空！");
		}
		DocFileEntity entity = checkDocFile(docFileDto.getFileId());
		if (entity.getFileName().equals(docFileDto.getFileName())) {
			return R.failed("名称修改前后一致，无需修改！");
		}
		docFileDto.setFolderFlag(entity.getFolderFlag());
		docFileDto.setOwnership(entity.getOwnership());
		docFileDto.setDelFlag(entity.getDelFlag());
		docFileDto.setParentId(entity.getParentId());
		checkFolderExist(docFileDto);
		checkUserRolePermission(entity.getEditFlag(), entity.getOwnership());
		docFileService.rename(docFileDto);
		return R.ok();
	}

	/**
	 * 回收
	 *
	 * @param docFiles
	 * @return
	 */
	@PostMapping("/recycle")
	public R recycle(@RequestBody List<DocFileDto> docFiles) {
		if (CollectionUtils.isEmpty(docFiles)) {
			return R.failed("未选择回收的文件！");
		}
		docFileService.recycle(docFiles);
		return R.ok();
	}

	/**
	 * 恢复
	 *
	 * @param docFiles
	 * @return
	 */
	@PostMapping("/recover")
	public R recover(@RequestBody List<DocFileDto> docFiles) {
		if (CollectionUtils.isEmpty(docFiles)) {
			return R.failed("未选择恢复的文件！");
		}
		docFileService.recover(docFiles);
		return R.ok();
	}

	/**
	 * 移动
	 *
	 * @param docFiles
	 * @param parentId
	 * @return
	 */
	@PostMapping("/move/{parentId}")
	public R move(@Valid @RequestBody List<DocFileDto> docFiles, @PathVariable Long parentId) {
		if (CollectionUtils.isEmpty(docFiles)) {
			return R.failed("未选择移动的文件！");
		}
		checkDocFolder(parentId);
		docFileService.move(parentId, docFiles);
		return R.ok();
	}

	/**
	 * 收藏
	 *
	 * @param fileId
	 * @return
	 */
	@PostMapping("/collect/{fileId}")
	public R collect(@PathVariable Long fileId) {
		checkDocFile(fileId);
		docCollectService.collect(fileId);
		return R.ok();
	}

	/**
	 * 取消收藏
	 *
	 * @param fileId
	 * @return
	 */
	@PostMapping("/collect/remove/{fileId}")
	public R removeCollect(@PathVariable Long fileId) {
		checkDocFile(fileId);
		docCollectService.removeByFileId(fileId);
		return R.ok();
	}

	/**
	 * 删除文件
	 *
	 * @param fileIds
	 * @return
	 */
	@PostMapping("/delete")
	public R deleteFile(@RequestBody List<Long> fileIds) {
		docFileService.deleteFile(fileIds);
		return R.ok();
	}
}
