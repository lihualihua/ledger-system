package com.digital.doc.helper;

import com.digital.admin.service.SysDeptService;
import com.digital.common.core.exception.ApplicationException;
import com.digital.common.core.exception.CheckedException;
import com.digital.common.security.service.PigUser;
import com.digital.doc.config.DocFileConfig;
import com.digital.model.admin.entity.SysDept;
import com.digital.model.admin.entity.SysRole;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserHelper {
    private static final Logger LOGGER = LogManager.getLogger(UserHelper.class);

    @Autowired
    protected DocFileConfig docFileConfig;

    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 获取用户ID
     *
     * @return
     */
    public Long getUserId() {
        PigUser user = getUser();
        if (Objects.isNull(user)) {
            return null;
        }
        return user.getId();
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public PigUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 匿名接口直接返回
        if (Objects.isNull(authentication) || authentication instanceof AnonymousAuthenticationToken) {
            LOGGER.warn("当前匿名登录，无用户信息");
            return null;
        }
        return (PigUser) authentication.getPrincipal();
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public void setAuthentication(Authentication authentication) {
        if (Objects.isNull(authentication)) {
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 获取部门ID
     *
     * @return
     */
    public Long getDeptId() {
        PigUser user = getUser();
        if (Objects.isNull(user)) {
            return null;
        }
        SysDept dept = user.getDept();
        if (Objects.isNull(dept)) {
            LOGGER.warn("当前用户无部门信息，username : {}", user.getUsername());
            return null;
        }
        return dept.getDeptId();
    }

    public SysDept getDept() {
        PigUser user = getUser();
        if (Objects.isNull(user)) {
            return null;
        }
        return user.getDept();
    }

    /**
     * 获取用户角色编码
     *
     * @return
     */
    public List<String> getUserRoleCodes() {
        PigUser user = getUser();
        if (Objects.isNull(user)) {
            return Collections.emptyList();
        }
        List<SysRole> roles = user.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        return roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }

    /**
     * 是否超级管理员
     *
     * @return
     */
    public boolean isSuperAdmin() {
        List<String> roleCodes = getUserRoleCodes();
        if (CollectionUtils.isEmpty(roleCodes)) {
            return false;
        }
        return roleCodes.contains(docFileConfig.getSuperAdminCode());
    }

    /**
     * 检查超级管理员权限
     */
    public void checkSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new ApplicationException("您无权限操作，请联系管理员!");
        }
    }

    /**
     * 根据ID查找部门
     *
     * @param deptId
     * @return
     */
    public SysDept findDeptById(Long deptId) {
        return sysDeptService.getById(deptId);
    }

    /**
     * 检查部门权限
     *
     * @param deptId
     */
    public void checkDeptPermission(Long deptId) {
        SysDept dept = findDeptById(deptId);
        if (Objects.isNull(dept)) {
            throw new CheckedException("当前部门不存在，请联系管理员!");
        }
        if (Objects.equals(deptId, getDeptId())) {
            return;
        }
        if (!isSuperAdmin()) {
            throw new CheckedException("您无该部门权限，请联系管理员!");
        }
    }
}
