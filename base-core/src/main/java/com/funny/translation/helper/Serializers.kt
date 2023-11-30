package com.funny.translation.helper

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import org.json.JSONObject
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 时间格式化，样式：2022-01-01 10:10:10
 */
object DateSerializerType1: KSerializer<Date> {
    private val simpleDateFormat by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    override val descriptor: SerialDescriptor
        = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        return simpleDateFormat.parse(decoder.decodeString()) ?: Date()
    }

    override fun serialize(encoder: Encoder, value: Date) {
        return encoder.encodeString(simpleDateFormat.format(value))
    }
}

/**
 * 时间格式化，样式：2022-01-01 10:10:10.123
 */
object DateSerializerType2: KSerializer<Date> {
    private val simpleDateFormat by lazy(LazyThreadSafetyMode.PUBLICATION) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    }

    override val descriptor: SerialDescriptor
            = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        return simpleDateFormat.parse(decoder.decodeString()) ?: Date()
    }

    override fun serialize(encoder: Encoder, value: Date) {
        return encoder.encodeString(simpleDateFormat.format(value))
    }
}

// BigDecimal
//@Serializer(forClass = BigDecimal::class)
//object BigDecimalSerializer : JsonTransformingSerializer<BigDecimal>(BigDecimal.serializer()) {
//    override fun transformDeserialize(element: JsonElement): JsonElement {
//        if (element is JsonPrimitive) {
//            val stringValue = element.content
//            return JsonElement(stringValue.toBigDecimal().toString())
//        }
//        return super.transformDeserialize(element)
//    }
//}、

object BigDecimalSerializer: KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor
            = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        return decoder.decodeString().toBigDecimal()
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        return encoder.encodeString(value.toString())
    }
}

//typealias BigDecimalSerializer = BigDecimalAsStringSerializer
object LenientBigDecimalSerializer : JsonTransformingSerializer<BigDecimal>(BigDecimalSerializer) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonPrimitive && !element.isString) {
            return JsonPrimitive(element.content)
        }
        return super.transformDeserialize(element)
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        if (element is JsonPrimitive && element.isString) {
            return JsonPrimitive(BigDecimal(element.content))
        }
        return super.transformSerialize(element)
    }
}
// Price
//object PriceSerializer: KSerializer<Price> {
//    override val descriptor: SerialDescriptor
//            = PrimitiveSerialDescriptor("Price", PrimitiveKind.STRING)
//
//    override fun deserialize(decoder: Decoder): Price {
//        return Price(decoder.decodeString())
//    }
//
//    override fun serialize(encoder: Encoder, value: Price) {
//        return encoder.encodeString(value.toString())
//    }
//}
typealias PriceSerializer = LenientBigDecimalSerializer


// JSONObject
object JSONObjectSerializer: KSerializer<JSONObject> {
    override val descriptor: SerialDescriptor
            = PrimitiveSerialDescriptor("JSONObject", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): JSONObject {
        return JSONObject(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: JSONObject) {
        return encoder.encodeString(value.toString())
    }
}