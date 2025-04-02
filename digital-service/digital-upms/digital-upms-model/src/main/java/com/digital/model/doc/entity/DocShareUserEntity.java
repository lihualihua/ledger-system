package com.digital.model.doc.entity;

import com.digital.model.admin.vo.UserVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分享用户信息")
@JsonIgnoreProperties(value = {"password", "salt", "createTime", "updateTime", "delFlag", "lockFlag", "phone", "avatar",
                                "deptId", "deptName", "roleList", "email"})
public class DocShareUserEntity extends UserVO {
    private static final long serialVersionUID = -2989853261725605904L;
}
