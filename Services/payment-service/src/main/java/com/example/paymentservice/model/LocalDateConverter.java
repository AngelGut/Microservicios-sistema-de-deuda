package com.example.paymentservice.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return attribute == null ? null : attribute.toString(); // yyyy-MM-dd
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LocalDate.parse(dbData);
    }
}
