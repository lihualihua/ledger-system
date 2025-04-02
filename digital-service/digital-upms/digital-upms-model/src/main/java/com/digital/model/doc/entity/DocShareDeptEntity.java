package com.digital.model.doc.entity;

import com.digital.model.admin.entity.SysDept;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分享部门信息")
@JsonIgnoreProperties(value = {"sortOrder", "createBy", "updateBy", "createTime", "updateTime", "parentId", "delFlag"})
public class DocShareDeptEntity extends SysDept {
    private static final long serialVersionUID = 3053477461130985275L;
}
