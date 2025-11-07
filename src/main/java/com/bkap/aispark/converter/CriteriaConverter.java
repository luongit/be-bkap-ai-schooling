package com.bkap.aispark.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.postgresql.util.PGobject;

@Converter(autoApply = true)
public class CriteriaConverter implements AttributeConverter<Map<String, BigDecimal>, PGobject> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PGobject convertToDatabaseColumn(Map<String, BigDecimal> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(mapper.writeValueAsString(attribute));
            return jsonObject;
        } catch (JsonProcessingException | SQLException e) {
            throw new IllegalArgumentException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, BigDecimal> convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null || dbData.getValue().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(dbData.getValue(), mapper.getTypeFactory()
                    .constructMapType(Map.class, String.class, BigDecimal.class));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to Map", e);
        }
    }
}
