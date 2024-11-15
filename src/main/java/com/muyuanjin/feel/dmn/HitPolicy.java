package com.muyuanjin.feel.dmn;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author muyuanjin
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum HitPolicy {
    UNIQUE("UNIQUE", false),
    FIRST("FIRST", false),
    PRIORITY("PRIORITY", false),
    ANY("ANY", false),
    COLLECT("COLLECT", true),
    RULE_ORDER("RULE ORDER", true),
    OUTPUT_ORDER("OUTPUT ORDER", true);

    private final String name;
    private final boolean multiHit;
}
