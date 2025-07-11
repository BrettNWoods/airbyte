/*
 * Copyright (c) 2024 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.cdk.load.data.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import io.airbyte.cdk.load.data.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.annotation.Property
import javax.inject.Singleton

@Singleton
class JsonSchemaToAirbyteType(
    @Property(name = "airbyte.destination.core.types.unions")
    private val unionBehavior: UnionBehavior,
) {
    enum class UnionBehavior {
        /**
         * Treat `{"type": [...]}` and `{"oneOf": [...]}` differently. In particular, the `type`
         * schema will be parsed into a [LegacyUnionType], whereas the `oneOf` schema will parse
         * into a [UnionType].
         */
        LEGACY,

        /**
         * Treat `{"type": [...]}` and `{"oneOf": [...]}` identically. Both will parse into
         * [UnionType].
         */
        DEFAULT,
    }

    private val log = KotlinLogging.logger {}

    fun convert(schema: JsonNode): AirbyteType = convertInner(schema)!!

    private fun convertInner(schema: JsonNode): AirbyteType? {
        // try {
        if (schema.isObject && schema.has("type")) {
            // Normal json object with {"type": ..., ...}
            val schemaType = (schema as ObjectNode).get("type")
            return if (schemaType.isTextual) {
                // {"type": <string>, ...}
                when (extractType(schema)) {
                    "string" -> fromString(schema)
                    "boolean" -> BooleanType
                    "int",
                    "integer" -> IntegerType
                    "number" -> fromNumber(schema)
                    "array" -> fromArray(schema)
                    "object" -> fromObject(schema)
                    "null" -> null
                    else -> UnknownType(schema)
                }
            } else if (schemaType.isArray) {
                // {"type": [...], ...}
                unionFromCombinedTypes(schemaType.toList(), schema)
            } else {
                UnknownType(schema)
            }
        } else if (schema.isObject && schema.has("\$ref")) {
            // TODO: Determine whether we even still need to support this
            return when (schema.get("\$ref").asText()) {
                "WellKnownTypes.json#/definitions/Integer" -> IntegerType
                "WellKnownTypes.json#/definitions/Number" -> NumberType
                "WellKnownTypes.json#/definitions/String" -> StringType
                "WellKnownTypes.json#/definitions/Boolean" -> BooleanType
                "WellKnownTypes.json#/definitions/Date" -> DateType
                "WellKnownTypes.json#/definitions/TimestampWithTimezone" ->
                    TimestampTypeWithTimezone
                "WellKnownTypes.json#/definitions/TimestampWithoutTimezone" ->
                    TimestampTypeWithoutTimezone
                "WellKnownTypes.json#/definitions/BinaryData" -> StringType
                "WellKnownTypes.json#/definitions/TimeWithTimezone" -> TimeTypeWithTimezone
                "WellKnownTypes.json#/definitions/TimeWithoutTimezone" -> TimeTypeWithoutTimezone
                else -> UnknownType(schema)
            }
        } else if (schema.isObject) {
            // {"oneOf": [...], ...} or {"anyOf": [...], ...} or {"allOf": [...], ...}
            val options = schema.get("oneOf") ?: schema.get("anyOf") ?: schema.get("allOf")
            return if (options != null) {
                if (options.isArray) {
                    // intentionally don't use the `unionOf()` utility method.
                    // We know this is a non-legacy union.
                    UnionType.of(
                        options.mapNotNull { convertInner(it) },
                        isLegacyUnion = false,
                    )
                } else {
                    // options is supposed to be a list, but fallback to sane behavior if it's not.
                    convertInner(options)
                }
            } else {
                // Default to object if no type and not a union type
                convertInner((schema as ObjectNode).put("type", "object"))
            }
        } else if (schema.isTextual) {
            // "<typename>"
            val typeSchema = JsonNodeFactory.instance.objectNode().put("type", schema.asText())
            return convertInner(typeSchema)
        } else {
            return UnknownType(schema)
        }
    }

    private fun extractType(schema: ObjectNode): String {
        val type = schema.get("type")
        if (type.isArray) {
            val types =
                schema.get("type").asIterable().map { it.asText() }.filter { it != "null" }.toList()
            if (types.size > 1) {
                throw IllegalArgumentException("Multiple types are not supported")
            }
            return types[0]
        } else {
            return type.asText()
        }
    }

    private fun fromString(schema: ObjectNode): AirbyteType =
        when (schema.get("format")?.asText()) {
            "date" -> DateType
            "time" ->
                if (schema.get("airbyte_type")?.asText() == "time_without_timezone") {
                    TimeTypeWithoutTimezone
                } else {
                    TimeTypeWithTimezone
                }
            "date-time" ->
                if (schema.get("airbyte_type")?.asText() == "timestamp_without_timezone") {
                    TimestampTypeWithoutTimezone
                } else {
                    TimestampTypeWithTimezone
                }
            null -> StringType
            else -> {
                log.warn { "Ignoring unrecognized string format: ${schema.get("format").asText()}" }
                StringType
            }
        }

    private fun fromNumber(schema: ObjectNode): AirbyteType =
        if (schema.get("airbyte_type")?.asText() == "integer") {
            IntegerType
        } else {
            NumberType
        }

    private fun fromArray(schema: ObjectNode): AirbyteType {
        val items = schema.get("items") ?: return ArrayTypeWithoutSchema
        if (items.isArray) {
            if (items.isEmpty) {
                return ArrayTypeWithoutSchema
            }
            val itemType = unionOf(items.mapNotNull { convertInner(it) })
            return ArrayType(FieldType(itemType, true))
        }
        return ArrayType(nodeToFieldType(items))
    }

    private fun fromObject(schema: ObjectNode): AirbyteType {
        val properties = schema.get("properties") ?: return ObjectTypeWithoutSchema
        if (properties.isEmpty) {
            return ObjectTypeWithEmptySchema
        }
        val propertiesMapped =
            properties
                .fields()
                .asSequence()
                .map { (name, node) -> name to nodeToFieldType(node) }
                .toMap(LinkedHashMap())
        val additionalProperties = schema.get("additionalProperties")?.asBoolean() ?: false
        val required: List<String> =
            schema.get("required")?.asSequence()?.map { it.asText() }?.toList()
                ?: emptyList<String>()
        return ObjectType(propertiesMapped, additionalProperties, required)
    }

    private fun fieldFromSchema(
        fieldSchema: ObjectNode,
    ): FieldType {
        val airbyteType = convertInner(fieldSchema) ?: UnknownType(fieldSchema)
        val nullable =
            fieldSchema.get("type")?.let {
                it.isArray && it.asIterable().any({ type -> type.asText() == "null" })
            }
                ?: true
        return FieldType(airbyteType, nullable = nullable)
    }

    private fun unionFromCombinedTypes(
        options: List<JsonNode>,
        parentSchema: ObjectNode
    ): AirbyteType {
        // Denormalize the properties across each type (the converter only checks what matters
        // per type).
        val unionOptions =
            options.mapNotNull {
                if (it.isTextual) {
                    val schema = parentSchema.deepCopy()
                    schema.put("type", it.textValue())
                    convertInner(schema)
                } else {
                    convertInner(it)
                }
            }
        if (unionOptions.isEmpty()) {
            return UnknownType(parentSchema)
        }
        return unionOf(unionOptions)
    }

    private fun unionOf(options: List<AirbyteType>) =
        when (unionBehavior) {
            UnionBehavior.LEGACY -> UnionType.of(options, isLegacyUnion = true)
            UnionBehavior.DEFAULT -> UnionType.of(options, isLegacyUnion = false)
        }

    private fun nodeToFieldType(node: JsonNode): FieldType =
        when (node) {
            is ObjectNode -> fieldFromSchema(node)
            else -> FieldType(UnknownType(node), nullable = true)
        }
}
