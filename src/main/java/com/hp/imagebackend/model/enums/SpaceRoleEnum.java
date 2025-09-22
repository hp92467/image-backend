package com.hp.imagebackend.model.enums;

import lombok.Getter;
import net.bytebuddy.asm.Advice;

import java.util.List;

// /model/enums/SpaceRoleEnum.java
@Getter
public enum SpaceRoleEnum {
    VIEWER("浏览者", "viewer"),
    EDITOR("编辑者", "editor"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    SpaceRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static SpaceRoleEnum getEnumByValue(String value) { /* ... */ }
    public static List<String> getAllTexts() { /* ... */ }
    public static List<String> getAllValues() { /* ... */ }

}