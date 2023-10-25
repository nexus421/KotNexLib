package kotnexlib

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Makes sure, that this variable will only be declared once and can't be changed afterward. Like a lateinit val.
 * Use it like: var test by Declare.once<String>()
 * Throws [IllegalStateException] if var was not initialized before get and [initialValue] is null.
 * Throws [IllegalStateException] if [throwOnChangeTry] is true and var is tried to change to another value than [initialValue].
 *
 * @param throwOnChangeTry if true (default) an exception will be thrown if you try to change this var a second time. Otherwise the setter will only be ignored without any additional information.
 * @param initialValue set this, if the default value should not be null. If this is not null, the getter never can throw an exception! Changing this var will only succeed if -> newValue != [initialValue]
 * @param onValueChanged if not null: Will be called after the var successfully changed
 *
 * @return [T] if not null or throws [IllegalStateException]
 */
fun <T : Any> Delegates.once(
    throwOnChangeTry: Boolean = true,
    initialValue: T? = null,
    onValueChanged: (() -> Unit)? = null
) = object : ReadWriteProperty<Any?, T> {
    private var value: T? = initialValue

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (this.value != initialValue) {
                if (throwOnChangeTry) throw IllegalStateException("Property ${property.name} cannot be set more than once.")
                else return
            }
            this.value = value
            onValueChanged?.invoke()
        }
    }

/**
 * Makes sure, that this variable will only be declared once and can't be changed afterward. Like a lateinit val.
 * Use it like: var test by Declare.onceOrNull<String>()
 *
 * WARNING: If you try to set null to this variable, it will throw a [IllegalStateException]!
 *
 * @param throwOnChangeTry if true (default) an exception will be thrown if you try to change this var a second time.
 *
 * @return [T] or null if not set/initialized yet.
 */
fun <T : Any> Delegates.onceOrNull(throwOnChangeTry: Boolean = true) = object : ReadWriteProperty<Any?, T?> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>) = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (this.value != null) {
            if (throwOnChangeTry) throw IllegalStateException("Property ${property.name} cannot be set more than once.")
            else return
        }
        if (value == null) throw IllegalStateException("onceOrNull is not allowed to be set to null!")
        this.value = value
    }
}
