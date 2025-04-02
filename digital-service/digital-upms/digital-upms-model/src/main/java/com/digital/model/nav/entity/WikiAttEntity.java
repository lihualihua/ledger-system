package com.digital.model.nav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "wiki附件")
@EqualsAndHashCode(callSuper = true)
@TableName("nav_wiki_att_t")
public class WikiAttEntity extends BaseEntity<WikiAttEntity> {
    private static final long serialVersionUID = 2144359211493919910L;

    @Schema(description = "编号")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "文件编号")
    private Long fileId;

    @Schema(description = "文件名称")
    @TableField(exist = false)
    private String fileName;

    @Schema(description = "wiki编号")
    private Long wikiId;
}
