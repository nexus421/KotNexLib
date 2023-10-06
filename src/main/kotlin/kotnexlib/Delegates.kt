package kotnexlib

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Makes sure, that this variable will only be declared once and can't be changed afterward. Like a lateinit val.
 * Use it like: var test by Declare.once<String>()
 *
 * @param throwOnChangeTry if true (default) an exception will be thrown if you try to change this var a second time.
 */
fun <T : Any> Delegates.once(throwOnChangeTry: Boolean = true): ReadWriteProperty<Any?, T> =
    object : ReadWriteProperty<Any?, T> {
        private var value: T? = null

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
        }


        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (this.value != null) {
                if (throwOnChangeTry) throw IllegalStateException("Property ${property.name} cannot be set more than once.")
                else return
            }
            this.value = value
        }
    }
