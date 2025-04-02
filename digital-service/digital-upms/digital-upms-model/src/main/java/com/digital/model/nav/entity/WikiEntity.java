package com.digital.model.nav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.List;

@Data
@Schema(description = "wiki")
@EqualsAndHashCode(callSuper = true)
@TableName("nav_wiki_t")
public class WikiEntity extends BaseEntity<WikiEntity> {
    private static final long serialVersionUID = 7804800606858819198L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "编码")
    private String code;

    @Setter
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    @TableField(value = "`order`")
    private Integer order;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "wiki版本")
    private String version;

    @Schema(description = "描述")
    @TableField("`desc`")
    private String desc;

    @Schema(description = "领域编号")
    private Long domainId;

    @Schema(description = "版本标记，1：最新版本，0：历史版本")
    private Integer versionFlag;

    @Schema(description = "文件夹编号，模板上传路径")
    private Long folderId;

    @Schema(description = "wiki附件列表")
    @TableField(exist = false)
    private List<WikiAttEntity> wikiAttList;

    @Schema(description = "历史版本列表")
    @TableField(exist = false)
    private List<WikiEntity> children;
}
