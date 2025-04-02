package com.digital.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digital.common.core.exception.CheckedException;
import com.digital.doc.helper.UserHelper;
import com.digital.doc.mapper.DocFunctionMapper;
import com.digital.doc.mapper.DocUserFunctionMapper;
import com.digital.doc.service.IDocFunctionService;
import com.digital.model.doc.dto.DocFunctionDto;
import com.digital.model.doc.entity.DocFunctionEntity;
import com.digital.model.doc.entity.DocUserFunctionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocFunctionServiceImpl extends ServiceImpl<DocFunctionMapper, DocFunctionEntity> implements IDocFunctionService {

    @Autowired
    private DocUserFunctionMapper userFunctionMapper;

    @Autowired
    private UserHelper userHelper;

    @Override
    public void saveFunction(DocFunctionDto functionDto) {
        checkUserPermission();
        DocFunctionEntity functionEntity = buildFunctionEntity(functionDto);
        DocFunctionEntity function = baseMapper.selectOne(Wrappers.<DocFunctionEntity>lambdaQuery().eq(
                DocFunctionEntity::getName, functionDto.getName()));
        if (Objects.nonNull(function)) {
            functionEntity.setId(function.getId());
            baseMapper.updateById(functionEntity);
        } else {
            functionEntity.setStatus(1);
            baseMapper.insert(functionEntity);
        }
    }

    private void checkUserPermission() {
        if (userHelper.isSuperAdmin()) {
            return;
        }
        throw new CheckedException("您无权限操作，请联系管理员!");
    }

    @Override
    public void deleteFunction(Long functionId) {
        checkUserPermission();
        baseMapper.deleteById(functionId);
        userFunctionMapper.delete(Wrappers.<DocUserFunctionEntity>lambdaQuery().eq(
                DocUserFunctionEntity::getFunctionId, functionId));
    }

    @Override
    public void bindingFunction(List<Long> functionIds) {
        Long userId = userHelper.getUserId();
        functionIds = functionIds.stream().distinct().collect(Collectors.toList());
        userFunctionMapper.delete(Wrappers.<DocUserFunctionEntity>lambdaQuery().eq(DocUserFunctionEntity::getUserId, userId));
        List<DocUserFunctionEntity> userFunctionList = new ArrayList<>();
        functionIds.forEach(item -> {
            DocUserFunctionEntity userFunctionEntity = new DocUserFunctionEntity();
            userFunctionEntity.setUserId(userId);
            userFunctionEntity.setFunctionId(item);
            userFunctionList.add(userFunctionEntity);
        });
        userFunctionMapper.insert(userFunctionList);
    }

    @Override
    public List<DocFunctionEntity> findAllFunction() {
        return baseMapper.selectList(Wrappers.<DocFunctionEntity>lambdaQuery().eq(
                DocFunctionEntity::getStatus, 1).orderByAsc(DocFunctionEntity::getOrder).orderByDesc(DocFunctionEntity::getUpdateTime));
    }

    @Override
    public List<DocFunctionEntity> findMyFunction() {
        return userFunctionMapper.findMyFunction(userHelper.getUserId());
    }

    private DocFunctionEntity buildFunctionEntity(DocFunctionDto functionDto) {
        DocFunctionEntity functionEntity = new DocFunctionEntity();
        functionEntity.setName(functionDto.getName());
        functionEntity.setIcon(functionDto.getIcon());
        functionEntity.setOrder(functionDto.getOrder());
        functionEntity.setPath(functionDto.getPath());
        functionEntity.setStatus(functionDto.getStatus());
        return functionEntity;
    }
}
