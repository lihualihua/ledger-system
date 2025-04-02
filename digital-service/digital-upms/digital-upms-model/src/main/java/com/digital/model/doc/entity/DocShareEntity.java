package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import com.digital.model.doc.contains.DocContains;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "分享记录表")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_share_t")
public class DocShareEntity extends BaseEntity<DocShareEntity> {
    private static final long serialVersionUID = 4797999845052108152L;

    /**
     * 编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分享编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "分享名称")
    private String name;

    @Schema(description = "用户编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "分享地址")
    private String shareUrl;

    @Schema(description = "有效期标识")
    private Integer validFlag;

    @Schema(description = "有效期")
    @JsonFormat(pattern = DocContains.DATE_FORMAT)
    private LocalDateTime validDate;

    @Schema(description = "md5")
    private String md5Value;

    @Schema(description = "描述")
    @TableField(value = "`desc`")
    private String desc;

    @Schema(description = "访问模式，1：内部公开（登录后就可以访问），2：指定用户或部门，3：密码访问")
    private Integer accessMode;

    @TableField(exist = false)
    @Schema(description = "分享用户列表")
    private List<DocShareUserEntity> shareUsers;

    @TableField(exist = false)
    @Schema(description = "分享部门列表")
    private List<DocShareDeptEntity> shareDepts;
}
