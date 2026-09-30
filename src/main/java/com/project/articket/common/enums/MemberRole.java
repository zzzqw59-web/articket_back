package com.project.articket.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {

    USER("USER", "USER"),
    STAFF("STAFF", "STAFF"),
    ADMIN("ADMIN", "ADMIN");

    private final String key;
    private final String title;

    public boolean equalsKey(String roleKey) {
        if (roleKey == null) return false;
        return this.key.equalsIgnoreCase(roleKey) || this.name().equalsIgnoreCase(roleKey);
    }
}
