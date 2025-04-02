package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.model.doc.entity.DocSearchEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
@Mapper
public interface DocSearchMapper extends BaseMapper<DocSearchEntity> {
    Page<DocSearchEntity> findSearchFiles(Page<DocSearchEntity> page, @Param("fileEntity") DocSearchEntity fileEntity, @Param("userId") Long userId);

    IPage<DocSearchEntity> findAllChildFiles(@Param("page") PageDTO<DocSearchEntity> page, @Param("fileEntity") DocSearchEntity fileEntity, @Param("userId") Long userId, @Param("deptId") Long deptId);

    IPage<DocSearchEntity> findChildFiles(PageDTO<DocSearchEntity> page, @Param("fileEntity") DocSearchEntity searchEntity, @Param("userId") Long userId, @Param("deptId") Long deptId);
}
