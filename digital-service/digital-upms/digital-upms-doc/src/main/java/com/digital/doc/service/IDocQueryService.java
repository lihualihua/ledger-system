package com.digital.doc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.vo.DocFileCountVO;
import com.digital.model.doc.vo.DocFileDetailVO;
import com.digital.model.doc.vo.DocFileSizeSumVO;

import java.util.List;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
public interface IDocQueryService extends IService<DocFileEntity> {

	/**
	 * 根据ID查找文件，关联用户和部门
	 *
	 * @param fileId
	 * @return
	 */
	DocFileEntity findDelById(Long fileId, boolean containDel);

	DocFileEntity findById(Long fileId);

	/**
	 * 根据ID查找文件，不关联用户和部门
	 *
	 * @param fileId
	 * @return
	 */
	DocFileEntity findByIdNoUser(Long fileId);

	/**
	 * 根据多个ID查找文件集合
	 *
	 * @param ids
	 * @return
	 */
	List<DocFileEntity> findByIds(List<Long> ids);

	/**
	 * 查找文件
	 *
	 * @param docFileDto
	 * @return
	 */
	DocFileEntity findDocFileEntity(DocFileDto docFileDto);

	/**
	 * 分页查找某一个文件夹下的文件列表，不跨层级
	 *
	 * @param docFileDto
	 * @param page
	 * @return
	 */
	IPage<DocFileEntity> findChildFiles(DocFileDto docFileDto, PageDTO<DocFileEntity> page);

	/**
	 * 分页查找某一个文件夹下的所有文件，包含所有层级
	 *
	 * @param docFileDto
	 * @param page
	 * @return
	 */
	IPage<DocFileEntity> findAllChildFiles(DocFileDto docFileDto, PageDTO<DocFileEntity> page);

	/**
	 * 分页查找已收藏的文件列表
	 *
	 * @param docFileDto
	 * @param page
	 * @return
	 */
	IPage<DocFileEntity> findCollectFiles(DocFileDto docFileDto, PageDTO<DocFileEntity> page);

	/**
	 * 根据父节点ID查找所有文件
	 *
	 * @param parentIds
	 * @param containSelf
	 * @param containDel
	 * @return
	 */
	List<DocFileEntity> findAllChildFiles(List<Long> parentIds, boolean containSelf, boolean containDel, boolean diffUser);

	/**
	 * 查找当前文件夹下的所有文件，不包含子级
	 *
	 * @param parentId
	 * @return
	 */
	List<DocFileEntity> findChildFilesByParentId(Long parentId);

	/**
	 * 分页查找
	 *
	 * @param parentId
	 * @return
	 */
	IPage<DocFileEntity> findChildFilesPageByParentId(Long parentId, PageDTO page);

	/**
	 * 查找文件夹/文件的大小
	 *
	 * @param docFileDto
	 * @return
	 */
	DocFileSizeSumVO findFileSizeSum(List<DocFileDto> docFileDto);

	/**
	 * 查找文件总数
	 *
	 * @return
	 */
	DocFileCountVO findFileCount();

	/**
	 * 查找文件/文件夹详细信息
	 *
	 * @return
	 */
	DocFileDetailVO findFileDetailInfo(Long fileId, boolean containSelf);
}
