package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "分享用户部门关联")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_share_user_dept_t")
public class UserDeptShareEntity extends BaseEntity<UserDeptShareEntity> {
    private static final long serialVersionUID = -8455391338562712550L;
    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "分享编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shareId;

    @Schema(description = "分享的目标用户id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shareUserId;

    @Schema(description = "分享的目标部门id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shareDeptId;
}
