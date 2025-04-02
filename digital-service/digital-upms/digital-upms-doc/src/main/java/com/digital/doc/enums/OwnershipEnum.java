package com.digital.doc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum OwnershipEnum {
    /**
     * 公共文档
     */
    COMMON("common", 1, "/公共文档"),

    /**
     * 我的文档
     */
    USER("user", 2, "/我的文档"),

    /**
     * 部门文档
     */
    DEPT("dept", 3, "/部门文档");

    /**
     * 名称
     */
    private final String name;

    /**
     * 值
     */
    private final int code;

    /**
     * 根路径
     */
    private final String rootPath;

    public static int getOwnership(String name) {
        for (OwnershipEnum e : OwnershipEnum.values()) {
            if (e.name.equals(name)) {
                return e.getCode();
            }
        }
        return 0;
    }

    public static List<Integer> getOwnershipValues() {
        return Arrays.stream(OwnershipEnum.values()).map(OwnershipEnum::getCode).collect(Collectors.toList());
    }

    public static String getRootPathByValue(int value) {
        for (OwnershipEnum e : OwnershipEnum.values()) {
            if (e.code == value) {
                return e.getRootPath();
            }
        }
        return null;
    }
}
