package kotnexlib.external

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A custom serializer for Kotlin's enum classes that serializes the enum values as integers.
 *
 *
 * Example usage:
 * class DemoEnumSerializer: EnumAsIntSerializer<DemoEnum>(
 *    "DemoEnum",
 *    { it.id },
 *    { v -> DemoEnum.entries.first { it.id == v } }
 * )
 *
 * Important: You need to implement org.jetbrains.kotlinx:kotlinx-serialization-json by yourself!!
 *
 * @param T the type of the enum being serialized
 * @param serialName the name of the serial descriptor for the enum
 * @param serialize a function that converts an enum value to its corresponding integer representation
 * @param deserialize a function that converts an integer representation back to its corresponding enum value
 */
open class EnumAsIntSerializer<T : Enum<*>>(
    serialName: String,
    val serialize: (T) -> Int,
    val deserialize: (Int) -> T
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeInt(serialize(value))

    override fun deserialize(decoder: Decoder): T = deserialize(decoder.decodeInt())
}


/**
 * A custom serializer for Kotlin's enum classes that serializes the enum values as String.
 *
 *
 * Example usage:
 * class DemoEnumSerializer: EnumAsStringSerializer<DemoEnum>(
 *    "DemoEnum",
 *    { it.id },
 *    { v -> DemoEnum.entries.first { it.id == v } }
 * )
 *
 * Important: You need to implement org.jetbrains.kotlinx:kotlinx-serialization-json by yourself!!
 *
 * @param T the type of the enum being serialized
 * @param serialName the name of the serial descriptor for the enum
 * @param serialize a function that converts an enum value to its corresponding String representation
 * @param deserialize a function that converts a String representation back to its corresponding enum value
 */
open class EnumAsStringSerializer<T : Enum<*>>(
    serialName: String,
    val serialize: (T) -> String,
    val deserialize: (String) -> T
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(serialize(value))

    override fun deserialize(decoder: Decoder) = deserialize(decoder.decodeString())
}

