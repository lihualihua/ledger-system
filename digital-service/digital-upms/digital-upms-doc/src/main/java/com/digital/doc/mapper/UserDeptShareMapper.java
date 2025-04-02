package com.digital.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digital.model.doc.entity.DocShareDeptEntity;
import com.digital.model.doc.entity.DocShareEntity;
import com.digital.model.doc.entity.DocShareUserEntity;
import com.digital.model.doc.entity.UserDeptShareEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档收藏
 */
@Mapper
public interface UserDeptShareMapper extends BaseMapper<UserDeptShareEntity> {

    List<DocShareUserEntity> findShareUserList(@Param("shareEntity") DocShareEntity shareEntity);

    List<DocShareDeptEntity> findShareDeptList(@Param("shareEntity") DocShareEntity shareEntity);
}
