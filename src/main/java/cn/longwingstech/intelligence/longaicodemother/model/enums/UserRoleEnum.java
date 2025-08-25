package cn.longwingstech.intelligence.longaicodemother.model.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (value==null) return null;
        UserRoleEnum[] values = UserRoleEnum.values();
        for (UserRoleEnum val : values) {
            if (val.value.equals(value)) {
                return val;
            }
        }
        return null;

    }

    public static String getValueByText(String text) {
        for (UserRoleEnum value : UserRoleEnum.values()) {
            if (value.getText().equals(text)) {
                return value.getValue();
            }
        }
        return null;
    }
}
