package com.funny.translation.helper

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

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