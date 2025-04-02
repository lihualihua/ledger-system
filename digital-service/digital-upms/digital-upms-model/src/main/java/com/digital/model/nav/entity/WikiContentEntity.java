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
@Schema(description = "wiki历史")
@EqualsAndHashCode(callSuper = true)
@TableName("nav_wiki_content_t")
public class WikiContentEntity extends BaseEntity<WikiContentEntity> {
    private static final long serialVersionUID = -5763758918816325961L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "标题")
    @TableField(exist = false)
    private String title;

    @Schema(description = "描述")
    @TableField(exist = false)
    private String desc;

    @Schema(description = "wiki编码")
    private String wikiCode;

    @Schema(description = "wiki内容")
    private String content;

    @Schema(description = "wiki原始内容，未格式化")
    private String originalContent;

    @Schema(description = "wiki版本")
    private String version;
}
