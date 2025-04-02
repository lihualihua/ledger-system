package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Schema(description = "部门文件")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_dept_file_t")
public class DeptFileEntity extends BaseEntity<DeptFileEntity> {
    private static final long serialVersionUID = -7062098848558724226L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "部门编号")
    private Long deptId;

    @Schema(description = "文件编号")
    private Long fileId;

    @Schema(description = "用户编号")
    private Long userId;
}
