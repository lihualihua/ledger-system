package com.digital.doc.helper;

import com.digital.common.core.exception.ApplicationException;
import com.digital.doc.config.DocFileConfig;
import com.digital.doc.enums.EditFlagEnum;
import com.digital.doc.enums.OwnershipEnum;
import com.digital.doc.service.IDocQueryService;
import com.digital.model.doc.entity.DocFileEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class UserPermissionHelper {

    @Autowired
    private IDocQueryService docQueryService;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private DocFileConfig docFileConfig;

    /**
     * 获取编辑标志
     *
     * @param ownership
     * @param parentId
     * @return
     */
    public Integer getEditFlag(Integer editFlagParam, Integer ownership, Long parentId) {
        if (Objects.nonNull(editFlagParam) && EditFlagEnum.getEditFlagEnumValues().contains(editFlagParam)) {
            return editFlagParam;
        }

        if (ownership == OwnershipEnum.COMMON.getCode()) {
            // 根目录下只能由管理员操作
            Integer editFlag = getParentEditFlag(parentId);
            if (parentId == 0L || Objects.isNull(editFlag)) {
                return EditFlagEnum.SUPER_ADMIN.getCode();
            }
            return editFlag;
        } else if (ownership == OwnershipEnum.USER.getCode()) {
            return EditFlagEnum.SELF.getCode();
        } else if (ownership == OwnershipEnum.DEPT.getCode()) {
            Integer editFlag = getParentEditFlag(parentId);
            if (parentId == 0L || Objects.isNull(editFlag)) {
                return EditFlagEnum.DEPT_ADMIN.getCode();
            }
            return editFlag;
        }
        throw new ApplicationException("服务器处理异常，文件归属不匹配!");
    }

    private Integer getParentEditFlag(Long parentId) {
        // 根节点取默认值
        if (parentId == 0L) {
            return null;
        }
        DocFileEntity entity = docQueryService.findById(parentId);
        if (Objects.isNull(entity)) {
            throw new ApplicationException("服务器处理异常，父级文件夹不存在!");
        }
        return entity.getEditFlag();
    }

    /**
     * 校验用户是否有权限
     *
     * @param editFlag
     * @param ownership
     */
    public void checkUserRolePermission(Integer editFlag, Integer ownership) {
        if (!hasPermission(editFlag, ownership)) {
            throw new ApplicationException("您无权限操作，请联系管理员!");
        }
    }

    /**
     * 校验用户是否有权限
     *
     * @param fileIds
     */
    public void checkUserRolePermission(List<Long> fileIds) {
        fileIds.forEach(item -> {
            DocFileEntity entity = docQueryService.findById(item);
            if (Objects.isNull(entity)) {
                return;
            }
            checkUserRolePermission(entity.getEditFlag(), entity.getOwnership());
        });
    }

    /**
     * 校验用户是否有权限
     *
     * @param editFlag
     * @param ownership
     * @return
     */
    public boolean hasPermission(Integer editFlag, Integer ownership) {
        // 个人文件不做校验
        if (ownership == OwnershipEnum.USER.getCode()) {
            return true;
        }

        List<String> userRoleCodes = userHelper.getUserRoleCodes();
        // 匿名访问或无角色
        if (CollectionUtils.isEmpty(userRoleCodes)) {
            return false;
        }
        if (ownership == OwnershipEnum.COMMON.getCode()) {
            if (Objects.isNull(editFlag) || editFlag.equals(EditFlagEnum.SUPER_ADMIN.getCode())) {
                String superAdminRoleCode = docFileConfig.getSuperAdminCode();
                return userRoleCodes.contains(superAdminRoleCode);
            } else if (editFlag.equals(EditFlagEnum.DEPT_ADMIN.getCode())) {
                // TODO 部门权限不做校验
//                return checkDeptEditFlag(userRoleCodes);
                return true;
            }
        } else if (ownership == OwnershipEnum.DEPT.getCode()) {
            // TODO 部门权限不做校验
//            return checkDeptEditFlag(userRoleCodes);
            return true;
        }
        return true;
    }

    private boolean checkDeptEditFlag(List<String> userRoleCodes) {
        String superAdminRoleCode = docFileConfig.getSuperAdminCode();
        String deptAdminRoleCode = docFileConfig.getDeptAdminCode();
        List<String> roleCodes = new ArrayList<>();
        roleCodes.add(superAdminRoleCode);
        roleCodes.add(deptAdminRoleCode);
        return userRoleCodes.stream().anyMatch(roleCodes::contains);
    }
}
