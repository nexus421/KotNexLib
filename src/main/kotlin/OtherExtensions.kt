package bayern.kickner.kotlin_extensions_android

import java.io.File
import java.nio.file.Files


/**
 * This gets the name of the current Class.
 * I use this for my Logs. So I always have an identical TAG to use
 */
inline val Any.TAG: String
    get() = javaClass.simpleName

/**
 * Ruft eine Methode auf, ohne die Methode direkt zu kennen.
 * Sucht die Methode anhand des methodName.
 * Diese Methode wird dann ausgeführt und das Ergebnis wird dann nach D geparst und returned.
 *
 * @param methodName Name der Methode die aufgerufen werden soll.
 * @param args Argumente, falls die aufzurufende Methode noch Parameter besitzt
 * @param D Rückgabewert der aufzurufenden Methode
 * @return Den Rückgabewert der aufgerufenen Methode oder null
 */
private fun <D> Any.callMethodByName(methodName: String, vararg args: Any?) = this.javaClass.getMethod(methodName).let { if (args.isEmpty()) it.invoke(this) else it.invoke(this, *args) } as? D?

/**
 * Castet this in C. Force-Cast. Wenn es fehlschlägt, gibt es eine Exception
 */
inline fun <reified C> Any.cast(): C = this as C

/**
 * Castet this in C. Wenn der Cast fehlschlägt, ist das Ergebnis null.
 */
inline fun <reified C> Any.safeCast(): C? = this as? C

inline fun <reified C, R> Any.letCast(block: (C) -> R): R = (this as C).let(block)

/**
 * Sucht für eine Variable den Getter und gibt das Ergebnis für den Getter zurück.
 * Das Ergebnis wird sofort nach D geparst. Bei einem Fehler wird null returned.
 *
 * Wichtig: Es muss einen public Getter geben!
 *
 * @param varName Name der Variable nach dessen Getter im aktuellen Objekt gesucht werden soll
 * @return Ergebnis des Getters. Null kann das Ergebnis des Getters, aber auch ein Fehler sein!
 */
private fun <D> Any.get(varName: String): D? {
    val getterName = "get${varName.replace(".", "").replaceFirstChar { it.toString().uppercase() }}"
    return callMethodByName<D>(getterName)
}

/**
 * Sucht für eine Variable den Setter und führt diesen mit dem entsprechenden valueToSet aus.
 *
 * Wichtig: Es muss einen public Setter geben!
 *
 * @param varName Name der Variable nach dessen Setter im aktuellen Objekt gesucht werden soll
 * @param valueToSet Dieser Wert wird über den Setter gesetzt.
 * @return Nichts, wie ein Setter eben auch
 */
private fun <D> Any.set(varName: String, valueToSet: D) {
    val setterName = "set${varName.replace(".", "").replaceFirstChar { it.toString().uppercase() }}"
    callMethodByName<D>(setterName, valueToSet)
}

