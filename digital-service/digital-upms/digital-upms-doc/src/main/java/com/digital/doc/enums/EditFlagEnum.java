package com.digital.doc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum EditFlagEnum {
    /**
     * 不可编辑
     */
    NOT_EDIT( 0),
    /**
     * 超级管理员
     */
    SUPER_ADMIN( 1),

    /**
     * 部门管理员
     */
    DEPT_ADMIN( 2),

    /**
     * 个人/自己
     */
    SELF(3),

    /**
     * 所有人
     */
    ALL(4);

    /**
     * 编码
     */
    private final Integer code;

    public static List<Integer> getEditFlagEnumValues() {
        return Arrays.stream(EditFlagEnum.values()).map(EditFlagEnum::getCode).collect(Collectors.toList());
    }
}
