package com.digital.doc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ValidFlagEnum {

    /**
     * 永久有效
     */
    PERMANENT("perpetuity", 1),

    /**
     * 一年
     */
    YEAR("year", 2),

    /**
     * 六个月
     */
    SIX_MONTH("sixMonth", 3),

    /**
     * 一个月
     */
    ONE_MONTH("oneMonth", 4),

    /**
     * 自定义
     */
    CUSTOM("custom", 5);

    /**
     * 名称
     */
    private final String name;

    /**
     * 值
     */
    private final int code;

    public static ValidFlagEnum getByCode(Integer code){
        for(ValidFlagEnum v:values()) {
            if(v.code==code) {
                return v;
            }
        }
        return null;
    }

    public static List<Integer> getValidFlagValues() {
        return Arrays.stream(ValidFlagEnum.values()).map(ValidFlagEnum::getCode).collect(Collectors.toList());
    }
}
