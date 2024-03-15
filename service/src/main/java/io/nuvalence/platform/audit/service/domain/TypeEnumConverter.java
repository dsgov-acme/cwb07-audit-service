package io.nuvalence.platform.audit.service.domain;

import io.nuvalence.platform.audit.service.domain.enums.TypeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Audit event entity.
 */
@Converter(autoApply = true)
public class TypeEnumConverter implements AttributeConverter<TypeEnum, String> {

    @Override
    public String convertToDatabaseColumn(TypeEnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public TypeEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TypeEnum.fromValue(dbData);
    }
}
