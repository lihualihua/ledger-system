package com.digital.ledger.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationEnum {

    MESSAGE(1),

    NOTIFICATION(2);

    private final Integer code;
}
