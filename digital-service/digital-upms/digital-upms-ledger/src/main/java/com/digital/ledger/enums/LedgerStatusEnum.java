package com.digital.ledger.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LedgerStatusEnum {
    PUBLISHED(1, "已发布"),

    ARCHIVED(2, "已归档"),

    VOIDED(3, "已作废");

    private final int code;

    private final String name;
}
