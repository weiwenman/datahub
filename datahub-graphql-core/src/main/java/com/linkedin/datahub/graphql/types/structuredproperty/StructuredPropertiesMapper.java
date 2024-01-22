package com.linkedin.datahub.graphql.types.structuredproperty;

import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.datahub.graphql.generated.EntityType;
import com.linkedin.datahub.graphql.generated.NumberValue;
import com.linkedin.datahub.graphql.generated.PropertyValue;
import com.linkedin.datahub.graphql.generated.StringValue;
import com.linkedin.datahub.graphql.generated.StructuredPropertiesEntry;
import com.linkedin.datahub.graphql.generated.StructuredPropertyEntity;
import com.linkedin.datahub.graphql.types.common.mappers.UrnToEntityMapper;
import com.linkedin.structured.StructuredProperties;
import com.linkedin.structured.StructuredPropertyValueAssignment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StructuredPropertiesMapper {

  public static final StructuredPropertiesMapper INSTANCE = new StructuredPropertiesMapper();

  public static com.linkedin.datahub.graphql.generated.StructuredProperties map(
      @Nonnull final StructuredProperties structuredProperties) {
    return INSTANCE.apply(structuredProperties);
  }

  public com.linkedin.datahub.graphql.generated.StructuredProperties apply(
      @Nonnull final StructuredProperties structuredProperties) {
    com.linkedin.datahub.graphql.generated.StructuredProperties result =
        new com.linkedin.datahub.graphql.generated.StructuredProperties();
    result.setProperties(
        structuredProperties.getProperties().stream()
            .map(this::mapStructuredProperty)
            .collect(Collectors.toList()));
    return result;
  }

  private StructuredPropertiesEntry mapStructuredProperty(
      StructuredPropertyValueAssignment valueAssignment) {
    StructuredPropertiesEntry entry = new StructuredPropertiesEntry();
    entry.setStructuredProperty(createStructuredPropertyEntity(valueAssignment));
    final List<PropertyValue> values = new ArrayList<>();
    final List<Entity> entities = new ArrayList<>();
    valueAssignment
        .getValues()
        .forEach(
            value -> {
              if (value.isString()) {
                this.mapStringValue(value.getString(), values, entities);
              } else if (value.isDouble()) {
                values.add(new NumberValue(value.getDouble()));
              }
            });
    entry.setValues(values);
    entry.setValueEntities(entities);
    return entry;
  }

  private StructuredPropertyEntity createStructuredPropertyEntity(
      StructuredPropertyValueAssignment assignment) {
    StructuredPropertyEntity entity = new StructuredPropertyEntity();
    entity.setUrn(assignment.getPropertyUrn().toString());
    entity.setType(EntityType.STRUCTURED_PROPERTY);
    return entity;
  }

  private void mapStringValue(
      String stringValue, List<PropertyValue> values, List<Entity> entities) {
    try {
      final Urn urnValue = Urn.createFromString(stringValue);
      entities.add(UrnToEntityMapper.map(urnValue));
    } catch (Exception e) {
      log.debug("String value is not an urn for this structured property entry");
    }
    values.add(new StringValue(stringValue));
  }
}
