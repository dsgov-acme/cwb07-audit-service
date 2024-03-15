package io.nuvalence.platform.audit.service.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Event data types.
 */
public enum TypeEnum {
    STATE_CHANGE_EVENT_DATA("StateChangeEventData"),

    ACTIVITY_EVENT_DATA("ActivityEventData");

    private final String value;

    TypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Constructs a new TypeEnum with the given string value.
     * @param value the string value to be converted to an enum value
     * @return an element from the enum
     * @throws IllegalArgumentException if the given string value doesn't match any enum values
     */
    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum typeEnum : TypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
