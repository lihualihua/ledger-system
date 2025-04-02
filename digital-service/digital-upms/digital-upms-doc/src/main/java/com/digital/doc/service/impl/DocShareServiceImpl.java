package com.digital.doc.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.doc.enums.ValidFlagEnum;
import com.digital.doc.helper.DictHelper;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.mapper.DocShareFileMapper;
import com.digital.doc.mapper.DocShareMapper;
import com.digital.doc.mapper.UserDeptShareMapper;
import com.digital.doc.service.IDocShareService;
import com.digital.model.doc.dto.DocShareDto;
import com.digital.model.doc.entity.DocFileEntity;
import com.digital.model.doc.entity.DocShareEntity;
import com.digital.model.doc.entity.DocShareFileEntity;
import com.digital.model.doc.entity.UserDeptShareEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocShareServiceImpl extends ServiceImpl<DocShareMapper, DocShareEntity> implements IDocShareService {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private UserDeptShareMapper userDeptShareMapper;

    @Autowired
    private DocShareFileMapper shareFileMapper;

    @Autowired
    private IDocShareService self;

    @Autowired
    private DictHelper dictHelper;

    @Autowired
    private DocQueryServiceImpl queryService;

    @Override
    public DocShareEntity saveShare(DocShareDto docShareDto, String tempContextUrl) {
        String hashValue = getMd5Hex(docShareDto);
        DocShareEntity queryShare = baseMapper.selectOne(Wrappers.<DocShareEntity>lambdaQuery().eq(
                DocShareEntity::getMd5Value, hashValue).eq(DocShareEntity::getUserId, userHelper.getUserId()));
        String shareUrl = dictHelper.getStringValue("share_config", "share_link_prefix") + hashValue;
        DocShareEntity docShareEntity = buildShareEntity(docShareDto, hashValue, shareUrl);
        // 设置访问模式
        setAccessMode(docShareDto, docShareEntity);
        boolean saveShareFileFlag = true;
        if (Objects.nonNull(queryShare)) {
            docShareEntity.setId(queryShare.getId());
            saveShareFileFlag = false;
        }
        this.saveOrUpdate(docShareEntity);
        // 更新分享信息时，不处理分享文件
        if (saveShareFileFlag) {
            saveShareFileEntity(docShareDto.getFileIds(), docShareEntity.getId());
        }
        saveUserDeptShareEntity(docShareEntity, docShareDto);
        return docShareEntity;
    }

    private void saveUserDeptShareEntity(DocShareEntity docShareEntity, DocShareDto docShareDto) {
        List<UserDeptShareEntity> entityList = buildUserDeptShareEntity(docShareEntity, docShareDto);
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        userDeptShareMapper.delete(Wrappers.<UserDeptShareEntity>lambdaQuery().eq(UserDeptShareEntity::getShareId, docShareEntity.getId()));
        userDeptShareMapper.insert(entityList);
    }

    private void setAccessMode(DocShareDto docShareDto, DocShareEntity docShareEntity) {
        Integer mode = docShareDto.getAccessMode();
        if (Objects.nonNull(mode) && 0 != mode) {
            docShareEntity.setAccessMode(mode);
            return;
        }

        if (CollectionUtils.isEmpty(docShareDto.getShareDeptIds()) && CollectionUtils.isEmpty(docShareDto.getShareUserIds())) {
            // 默认为内部公开
            docShareEntity.setAccessMode(1);
        } else {
            docShareEntity.setAccessMode(2);
        }
    }

    private void saveShareFileEntity(List<Long> fileIds, Long shareId) {
        List<DocShareFileEntity> shareFiles = new ArrayList<>();
        fileIds.forEach(item -> {
            DocShareFileEntity shareFileEntity = new DocShareFileEntity();
            shareFileEntity.setFileId(item);
            shareFileEntity.setShareId(shareId);
            shareFiles.add(shareFileEntity);
        });
        shareFileMapper.insert(shareFiles);
    }

    private String getMd5Hex(DocShareDto docShareDto) {
        List<Long> fileIds = docShareDto.getFileIds();
        fileIds = fileIds.stream().distinct().sorted().collect(Collectors.toList());
        String fileIdStr = String.join(",", fileIds.toString());
        return DigestUtil.md5Hex16(fileIdStr);
    }

    @Override
    public int deleteShare(Long shareId) {
        userDeptShareMapper.delete(Wrappers.<UserDeptShareEntity>lambdaQuery().eq(UserDeptShareEntity::getShareId, shareId));
        shareFileMapper.delete(Wrappers.<DocShareFileEntity>lambdaQuery().eq(DocShareFileEntity::getShareId, shareId));
        return baseMapper.deleteById(shareId);
    }

    @Override
    public DocShareEntity findByMd5Value(String md5Value) {
        return baseMapper.selectOne(Wrappers.<DocShareEntity>lambdaQuery().eq(
                DocShareEntity::getMd5Value, md5Value));
    }

    @Override
    public List<DocFileEntity> findFilesByMd5Value(DocShareDto shareDto) {
        String md5Value = shareDto.getMd5Value();
        DocShareEntity shareEntity = findByMd5Value(md5Value);
        if (Objects.isNull(shareEntity)) {
            return Collections.emptyList();
        }

        // 内部公开和分享人 直接查看分享文件
        if (Objects.equals(shareEntity.getAccessMode(), 1) || Objects.equals(shareEntity.getUserId(), userHelper.getUserId())) {
            List<DocFileEntity> fileList = shareFileMapper.findFilesByMd5Value(shareEntity);
            return findShareFilesByParentId(shareDto.getParentId(), fileList);
        }

        List<UserDeptShareEntity> userDeptShareEntityList = findUserDeptShareEntityList(shareEntity);
        if (CollectionUtils.isEmpty(userDeptShareEntityList)) {
            return Collections.emptyList();
        }
        return findShareFilesByParentId(shareDto.getParentId(), shareFileMapper.findFilesByMd5Value(shareEntity));
    }

    private List<DocFileEntity> findShareFilesByParentId(Long parentId, List<DocFileEntity> fileList) {
        if (parentId == null) {
            return fileList;
        }
        if (CollectionUtils.isEmpty(fileList)) {
            return Collections.emptyList();
        }
        // 检查parentId是否属于分享的文件夹下面
        List<Long> parentIds = fileList.stream().filter(item -> item.getFolderFlag() == 1).map(
                DocFileEntity::getFileId).collect(Collectors.toList());
        List<Long> fileIds = queryService.findAllChildFiles(parentIds, true, false, false).stream().map(
                DocFileEntity::getFileId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fileIds) || !fileIds.contains(parentId)) {
            return Collections.emptyList();
        }
        return queryService.findChildFilesByParentId(parentId);
    }

    private List<UserDeptShareEntity> findUserDeptShareEntityList(DocShareEntity shareEntity) {
        return userDeptShareMapper.selectList(Wrappers.<UserDeptShareEntity>lambdaQuery().eq(
                UserDeptShareEntity::getShareId, shareEntity.getId()).and(item -> item.eq(UserDeptShareEntity::getShareUserId,
                userHelper.getUserId()).or().eq(UserDeptShareEntity::getShareDeptId, userHelper.getDeptId())));
    }

    @Override
    public void deleteExpiredShare() {
        List<DocShareEntity> shareEntityList = baseMapper.selectList(Wrappers.<DocShareEntity>lambdaQuery().lt(DocShareEntity::getValidDate, DateUtil.today()));
        if (CollectionUtils.isEmpty(shareEntityList)) {
            return;
        }
        shareEntityList.forEach(item -> {
            self.deleteShare(item.getId());
        });
    }

    @Override
    public IPage<DocShareEntity> findMyShare(DocShareDto shareDto, PageDTO<DocShareEntity> page) {
        LambdaQueryWrapper<DocShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocShareEntity::getUserId, userHelper.getUserId());
        queryWrapper.orderByDesc(DocShareEntity::getUpdateTime);
        IPage<DocShareEntity> shareEntityPage = baseMapper.selectPage(page, queryWrapper);
        if (CollectionUtils.isEmpty(shareEntityPage.getRecords())) {
            return shareEntityPage;
        }
        shareEntityPage.getRecords().forEach(item -> {
            // 查找用户数据
            item.setShareUsers(userDeptShareMapper.findShareUserList(item));

            // 查找部门数据
            item.setShareDepts(userDeptShareMapper.findShareDeptList(item));
        });
        return shareEntityPage;
    }

    @Override
    public IPage<DocShareEntity> findShareWithMe(DocShareDto shareDto, PageDTO<DocShareEntity> page) {
        LambdaQueryWrapper<UserDeptShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDeptShareEntity::getShareUserId, userHelper.getUserId()).or().eq(
                UserDeptShareEntity::getShareDeptId, userHelper.getDeptId());
        List<UserDeptShareEntity> userDeptShareEntityList = userDeptShareMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(userDeptShareEntityList)) {
            return page;
        }
        Set<Long> shareIds = userDeptShareEntityList.stream().map(UserDeptShareEntity::getShareId).collect(Collectors.toSet());
        return baseMapper.selectPage(page, Wrappers.<DocShareEntity>lambdaQuery().in(
                CollectionUtils.isNotEmpty(shareIds),DocShareEntity::getId, shareIds).orderByDesc(DocShareEntity::getUpdateTime));
    }

    @Override
    public void deleteShareByFileIds(List<Long> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        List<DocShareFileEntity> shareFileEntityList = shareFileMapper.selectList(
                Wrappers.<DocShareFileEntity>lambdaQuery().in(DocShareFileEntity::getFileId, fileIds));
        if (CollectionUtils.isEmpty(shareFileEntityList)) {
            return;
        }
        Set<Long> shareIds = shareFileEntityList.stream().map(DocShareFileEntity::getShareId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(shareIds)) {
            return;
        }
        shareIds.forEach(this::deleteShare);
    }

    @Override
    public DocShareEntity findShareDetail(Long shareId) {
        DocShareEntity shareEntity = baseMapper.selectOne(Wrappers.<DocShareEntity>lambdaQuery().eq(DocShareEntity::getId, shareId));
        if (Objects.nonNull(shareEntity) && Objects.equals(shareEntity.getUserId(), userHelper.getUserId())) {
            // 查找用户数据
            shareEntity.setShareUsers(userDeptShareMapper.findShareUserList(shareEntity));

            // 查找部门数据
            shareEntity.setShareDepts(userDeptShareMapper.findShareDeptList(shareEntity));
        }

        return shareEntity;
    }

    private DocShareEntity buildShareEntity(DocShareDto docShareDto, String md5Value, String shareUrl) {
        DocShareEntity docShareEntity = new DocShareEntity();
        docShareEntity.setUserId(userHelper.getUserId());
        docShareEntity.setName(docShareDto.getName());
        docShareEntity.setShareUrl(shareUrl);
        docShareEntity.setValidFlag(docShareDto.getValidFlag());
        docShareEntity.setDesc(docShareDto.getDesc());
        docShareEntity.setMd5Value(md5Value);
        setValidDate(docShareDto, docShareEntity);
        return docShareEntity;
    }

    private void setValidDate(DocShareDto docShareDto, DocShareEntity docShareEntity) {
        switch (Objects.requireNonNull(ValidFlagEnum.getByCode(docShareDto.getValidFlag()))) {
            case CUSTOM:
                docShareEntity.setValidDate(DateUtil.toLocalDateTime(DateUtil.parse(docShareDto.getValidDate())));
                break;
            case PERMANENT:
                docShareEntity.setValidDate(null);
                break;
            case YEAR:
                docShareEntity.setValidDate(DateUtil.offsetYear(new Date(), 1).toLocalDateTime());
                break;
            case SIX_MONTH:
                docShareEntity.setValidDate(DateUtil.offsetMonth(new Date(), 6).toLocalDateTime());
                break;
            case ONE_MONTH:
                docShareEntity.setValidDate(DateUtil.offsetMonth(new Date(), 1).toLocalDateTime());
                break;
            default:
                break;
        }
    }

    private List<UserDeptShareEntity> buildUserDeptShareEntity(DocShareEntity docShareEntity, DocShareDto docShareDto) {
        // 内部公开访问模式，不关联用户或部门
        if (docShareEntity.getAccessMode() == 1) {
            return Collections.emptyList();
        }

        List<Long> shareUserIds = docShareDto.getShareUserIds();
        List<Long> shareDeptIds = docShareDto.getShareDeptIds();
        List<UserDeptShareEntity> userDeptShareEntities = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(shareUserIds)) {
            shareUserIds.forEach(shareUserId -> {
                userDeptShareEntities.add(buildUserShare(docShareEntity.getId(), shareUserId));
            });
        }

        if (CollectionUtils.isNotEmpty(shareDeptIds)) {
            shareDeptIds.forEach(shareDeptId -> {
                userDeptShareEntities.add(buildDeptShare(docShareEntity.getId(), shareDeptId));
            });
        }

        return userDeptShareEntities;
    }

    private UserDeptShareEntity buildUserShare(Long shareId, Long shareUserId) {
        UserDeptShareEntity userDeptShareEntity = new UserDeptShareEntity();
        userDeptShareEntity.setShareId(shareId);
        userDeptShareEntity.setShareUserId(shareUserId);
        return userDeptShareEntity;
    }

    private UserDeptShareEntity buildDeptShare(Long shareId, Long shareDeptId) {
        UserDeptShareEntity userDeptShareEntity = new UserDeptShareEntity();
        userDeptShareEntity.setShareId(shareId);
        userDeptShareEntity.setShareDeptId(shareDeptId);
        return userDeptShareEntity;
    }
}
