package com.digital.ledger.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum LedgerTypeEnum {
    ROW_TEMPLATE(1, "rowTemplate"),

    CELL_TEMPLATE(2, "cellTemplate");

    private final Integer type;

    private final String name;

    public static String getNameByType(Integer type) {
        for (LedgerTypeEnum enums : values()) {
            if (Objects.equals(type, enums.getType())) {
                return enums.getName();
            }
        }
        return null;
    }
}
