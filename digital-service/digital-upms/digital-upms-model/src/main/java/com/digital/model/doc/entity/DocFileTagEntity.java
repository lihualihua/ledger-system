package com.digital.model.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.digital.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "文档标签")
@EqualsAndHashCode(callSuper = true)
@TableName("doc_file_tag_t")
public class DocFileTagEntity extends BaseEntity<DocFileTagEntity> {
    private static final long serialVersionUID = 5450526498294367322L;
    /**
     * 编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "标签编号")
    private Long tagId;

    @Schema(description = "文件编号")
    private Long fileId;

    @Schema(description = "文件所属部门编号")
    private Long deptId;
}
