package com.digital.doc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.enums.OwnershipEnum;
import com.digital.doc.helper.DictHelper;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.mapper.DeptFileMapper;
import com.digital.doc.mapper.DocFileTagMapper;
import com.digital.doc.mapper.DocTagMapper;
import com.digital.doc.service.IDocQueryService;
import com.digital.doc.service.IDocTagService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.dto.DocFileTagDto;
import com.digital.model.doc.dto.DocTagDto;
import com.digital.model.doc.entity.DeptFileEntity;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.entity.DocFileTagEntity;
import com.digital.model.doc.entity.DocTagEntity;
import com.digital.model.doc.response.DocTagResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocTagServiceImpl extends ServiceImpl<DocTagMapper, DocTagEntity> implements IDocTagService {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private DocFileTagMapper fileTagMapper;

    @Autowired
    private IDocQueryService docQueryService;

    @Autowired
    private DictHelper dictHelper;

    @Autowired
    private DeptFileMapper deptFileMapper;

    @Override
    public DocTagEntity saveTag(DocTagDto docTagDto) {
        if (StringUtils.isBlank(docTagDto.getName())) {
            throw new CheckedException("标签名称不能为空!");
        }
        DocTagEntity tagEntity = buildTagEntity(docTagDto);
        checkMaxTagSize(tagEntity);

        DocTagEntity tagResult = baseMapper.selectOne(Wrappers.<DocTagEntity>lambdaQuery().eq(
                DocTagEntity::getName, tagEntity.getName()).eq(DocTagEntity::getUserId, tagEntity.getUserId()));
        if (Objects.nonNull(tagResult) ) {
            throw new CheckedException("该标签名称已存在，请勿重复添加!");
        }

        if (Objects.nonNull(tagEntity.getId())) {
            baseMapper.updateById(tagEntity);
        } else {
            tagEntity.setStatus(1);
            baseMapper.insert(tagEntity);
        }
        return tagEntity;
    }

    private void checkMaxTagSize(DocTagEntity tagEntity) {
        // 超级管理员无限制，普通用户默认100个
        if (userHelper.isSuperAdmin()) {
            return;
        }
        List<DocTagEntity> tags = baseMapper.selectList(Wrappers.<DocTagEntity>lambdaQuery().eq(DocTagEntity::getUserId, tagEntity.getUserId()));
        int maxTagSize = dictHelper.getIntValue("tagConfig", "maxTagSize", 100);
        if (CollectionUtils.isNotEmpty(tags) && tags.size() > maxTagSize) {
            throw new CheckedException("添加标签失败，个人标签最多添加 "+ maxTagSize +" 个!");
        }
    }

    @Override
    public int deleteTag(Long tagId) {
        fileTagMapper.delete(Wrappers.<DocFileTagEntity>lambdaQuery().eq(DocFileTagEntity::getTagId, tagId));
        return baseMapper.deleteById(tagId);
    }

    @Override
    public void saveTags2Files(DocFileTagDto fileTagDto) {
        List<Long> fileIds = fileTagDto.getFileIds();
        List<Long> tagIds = fileTagDto.getTagIds();
        deleteUserFileTag(fileIds);
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<DocTagEntity> tagEntityList = baseMapper.selectList(Wrappers.<DocTagEntity>lambdaQuery().eq(DocTagEntity::getUserId,
                userHelper.getUserId()).in(DocTagEntity::getId, tagIds));
        if (CollectionUtils.isEmpty(tagEntityList)) {
            return;
        }
        List<Long> tagIdsResult = tagEntityList.stream().map(DocTagEntity::getId).collect(Collectors.toList());
        // 查找文件归属
        List<DocFileEntity> fileEntityList = docQueryService.findByIds(fileIds);
        if (CollectionUtils.isEmpty(fileEntityList)) {
            return;
        }
        Map<Integer, List<DocFileEntity>> fileEntityMap = fileEntityList.stream().collect(Collectors.groupingBy(DocFileEntity::getOwnership));
        // 更新标签公开标记
        if (CollectionUtils.isNotEmpty(fileEntityMap.get(OwnershipEnum.COMMON.getCode()))) {
            updatePublicFlag(tagIdsResult, OwnershipEnum.COMMON.getCode());
        } else if (CollectionUtils.isNotEmpty(fileEntityMap.get(OwnershipEnum.DEPT.getCode()))) {
            updatePublicFlag(tagIdsResult, OwnershipEnum.DEPT.getCode());
        }
        fileIds.forEach(fileId -> fileTagMapper.insert(buildFileTagEntity(fileId, tagIdsResult)));
    }

    private void updatePublicFlag(List<Long> tagIdsResult, int val) {
        LambdaUpdateWrapper<DocTagEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(DocTagEntity::getId, tagIdsResult);
        wrapper.set(DocTagEntity::getPublicFlag, val);
        baseMapper.update(wrapper);
    }

    private void deleteUserFileTag(List<Long> deleteFileIds) {
        if (CollectionUtils.isEmpty(deleteFileIds)) {
            return;
        }
        List<DocTagEntity> tagEntityList = findMyTags();
        if (CollectionUtils.isEmpty(tagEntityList)) {
            return;
        }
        List<Long> tagIds = tagEntityList.stream().map(DocTagEntity::getId).collect(Collectors.toList());
        deleteFileIds.forEach(fileId -> {
            LambdaQueryWrapper<DocFileTagEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocFileTagEntity::getFileId, fileId);
            queryWrapper.in(DocFileTagEntity::getTagId, tagIds);
            fileTagMapper.delete(queryWrapper);
        });
    }

    @Override
    public List<DocTagEntity> findMyTags() {
        return baseMapper.selectList(Wrappers.<DocTagEntity>lambdaQuery().eq(DocTagEntity::getUserId,
                userHelper.getUserId()));
    }

    @Override
    public Page<DocTagResponse> findPublicTags(DocTagDto docTagDto, Page page) {
        return fileTagMapper.findPublicTags(page, docTagDto, userHelper.getDeptId());
    }

    @Override
    public IPage<DocFileEntity> findFilesByTag(DocTagDto docTagDto, PageDTO page) {
        if (Objects.isNull(docTagDto.getId()) && StringUtils.isBlank(docTagDto.getName())) {
            return new Page<>();
        }
        // 查找标签的根路径下文件
        Long parentId = docTagDto.getParentId();
        if (Objects.isNull(parentId)) {
            return fileTagMapper.findFilesByTag(page, buildTagEntity(docTagDto), docTagDto.getFileName());
        }
        DocFileEntity entity = docQueryService.findDelById(parentId, false);
        if (Objects.isNull(entity)) {
            return new Page<>();
        }
        DocFileDto fileDto = new DocFileDto();
        fileDto.setOwnership(entity.getOwnership());
        fileDto.setParentId(parentId);
        fileDto.setFileName(docTagDto.getFileName());
        fileDto.setDelFlag(entity.getDelFlag());
        fileDto.setDeptId(userHelper.getDeptId());
        return docQueryService.findChildFiles(fileDto, page);
    }

    @Override
    public void deleteFilesWithTag(List<Long> fileIds) {
        LambdaQueryWrapper<DocFileTagEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DocFileTagEntity::getFileId, fileIds);
        fileTagMapper.delete(queryWrapper);
    }

    @Override
    public List<DocTagEntity> findTagByFileId(Long fileId) {
        return fileTagMapper.findTagByFileId(fileId, userHelper.getUserId());
    }

    private DocTagEntity buildTagEntity(DocTagDto docTagDto) {
        DocTagEntity tagEntity = new DocTagEntity();
        Long tagId = docTagDto.getId();
        if (Objects.nonNull(tagId)) {
            tagEntity.setId(tagId);
        }
        tagEntity.setName(docTagDto.getName());
        tagEntity.setDesc(docTagDto.getDesc());
        tagEntity.setStatus(docTagDto.getStatus());
        tagEntity.setUserId(userHelper.getUserId());
        tagEntity.setPublicFlag(0);
        return tagEntity;
    }

    private List<DocFileTagEntity> buildFileTagEntity(Long fileId, List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        Long deptId;
        DeptFileEntity deptFileEntity = deptFileMapper.selectOne(Wrappers.<DeptFileEntity>lambdaQuery().eq(DeptFileEntity::getFileId, fileId));
        if (Objects.nonNull(deptFileEntity)) {
            deptId = deptFileEntity.getDeptId();
        } else {
            deptId = null;
        }
        List<DocFileTagEntity> fileTags = new ArrayList<>();
        tagIds.forEach(item -> {
            DocFileTagEntity fileTagEntity = new DocFileTagEntity();
            fileTagEntity.setDeptId(deptId);
            fileTagEntity.setFileId(fileId);
            fileTagEntity.setTagId(item);
            fileTags.add(fileTagEntity);
        });
        return fileTags;
    }
}
