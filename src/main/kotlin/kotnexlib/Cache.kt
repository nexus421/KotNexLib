package kotnexlib

import kotlinx.coroutines.*
import kotnexlib.LocalCache.cacheKey
import kotnexlib.LocalCache.getOrSet
import kotnexlib.LocalCache.initCache
import kotnexlib.LocalCache.setData
import kotnexlib.LocalCache.theCache
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

/**
 * Global, type-based in-memory cache.
 *
 * Objects of arbitrary types can be stored here and retrieved at any time,
 * regardless of which provider or implementation is currently active.
 * Each type automatically receives its own [Cache] bucket, identified by the
 * fully qualified class name ([cacheKey]).
 *
 * The outer map [theCache] is thread-safe ([ConcurrentHashMap]). The inner
 * [Cache] instances are as well (see the [Cache] class).
 *
 * **Optional pre-configuration:** With [initCache] a bucket can be initialized
 * before first use with a configured [Cache] instance (e.g. with TTL).
 * Without [initCache], a `Cache()` with default settings is created automatically
 * on the first access.
 */
object LocalCache {

    /**
     * Internal map that maps each type to its own [Cache] bucket.
     * The key is the fully qualified class name (see [cacheKey]).
     */
    val theCache = ConcurrentHashMap<String, Cache<String, Any>>()

    /**
     * Returns the cache key for type [T].
     *
     * [KClass.qualifiedName] is used as the key, which uniquely identifies each type.
     * Local and anonymous classes are not allowed, as they have no stable qualified name.
     *
     * @throws IllegalArgumentException if [T] is a local or anonymous class.
     */
    inline fun <reified T> cacheKey() =
        T::class.qualifiedName ?: throw IllegalArgumentException("Local and anonymous classes are not allowed.")

    /**
     * Initializes the bucket for type [T] with a configured [Cache] instance.
     *
     * Intended for application startup to set e.g. TTL or an eviction callback
     * for a specific type. Calling this is not required — without [initCache],
     * a `Cache()` with default settings will be used automatically on the first
     * [setData] or [getOrSet] call.
     *
     * @param cache The pre-configured [Cache] instance for type [T].
     * @throws IllegalStateException if a bucket for [T] already exists.
     */
    inline fun <reified T> initCache(cache: Cache<String, Any>) {
        check(theCache.putIfAbsent(cacheKey<T>(), cache) == null) {
            "Cache for ${T::class.qualifiedName} has already been initialized."
        }
    }

    /**
     * Returns the cached value for [key] of type [T],
     * or `null` if no entry is present.
     *
     * @param key Key of the entry to look up.
     */
    inline fun <reified T> getData(key: String): T? =
        theCache[cacheKey<T>()]?.get(key)?.safeCast()

    /**
     * Stores [value] under [key] in the bucket for type [T].
     *
     * If no bucket for [T] exists yet, a `Cache()` with default settings is created
     * automatically. An existing entry for [key] will be overwritten.
     *
     * @param key Key under which the value is stored.
     * @param value The value to store.
     */
    inline fun <reified T : Any> setData(key: String, value: T) {
        theCache.getOrPut(cacheKey<T>()) { Cache() }.set(key, value)
    }

    /**
     * Returns the cached value for [key] of type [T]. If no entry is present,
     * [loader] is called, the result is stored and returned.
     *
     * If no bucket for [T] exists yet, a `Cache()` with default settings is created automatically.
     *
     * @param key Key of the entry to look up.
     * @param loader Suspend function that computes or loads the value if no cache hit is found.
     * @return The cached or freshly loaded value.
     */
    suspend inline fun <reified T : Any> getOrSet(
        key: String,
        noinline loader: suspend () -> T
    ): T = theCache.getOrPut(cacheKey<T>()) { Cache() }.getOrSet(key, loader).safeCast()!!

    /**
     * Returns the entire [Cache] bucket for type [T],
     * or `null` if no bucket for this type exists yet.
     */
    inline fun <reified T> getCacheFor(): Cache<String, Any>? =
        theCache[cacheKey<T>()]

}


/**
 * Generic, thread-safe cache with optional automatic cleanup.
 *
 * Entries are stored under a freely chosen key [K] and return values of type [T].
 * Internally, a timestamp is stored for each entry, which is used for time-based cleanup.
 *
 * **Possible extension:** Refresh-on-Access – the timestamp of an entry is reset on every
 * [get] call, so the TTL counts from the last access rather than from insertion.
 * Not currently implemented.
 *
 * @param K Type of the key (e.g. `String` for a customer ID).
 * @param T Type of the stored value (e.g. `CustomerData`).
 * @param maxAge Optional maximum age of an entry. Used as the default value
 *   for [cleanup] and [startAutoCleanup].
 * @param checkInterval Optional check interval for automatic cleanup.
 *   If a value is provided, the automatic cleanup job starts immediately when the
 *   cache is created. Defaults to [maxAge] if provided.
 * @param onEvict Optional callback invoked whenever an entry is removed from the
 *   cache – both by [cleanup] and by [remove] and [clear].
 *   Useful for releasing resources (e.g. closing connections) or logging evictions.
 * @param scope [CoroutineScope] in which the automatic cleanup job runs.
 *   Can be replaced with a custom scope for testing.
 */
class Cache<K, T>(
    val maxAge: Duration? = null,
    checkInterval: Duration? = maxAge,
    private val onEvict: ((key: K, value: T) -> Unit)? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    /**
     * Internal wrapper that stores a cached value together with its
     * insertion timestamp (Unix milliseconds).
     */
    private data class CacheEntry<T>(val value: T, val timestamp: Long = System.currentTimeMillis())

    private val data = ConcurrentHashMap<K, CacheEntry<T>>()
    private var cleanupJob: Job? = null

    /** Number of entries currently stored in the cache. */
    val size: Int get() = data.size

    /**
     * `true` if the cache contains no entries.
     * */
    val isEmpty: Boolean get() = data.isEmpty()

    init {
        if (maxAge != null && checkInterval != null) {
            startAutoCleanup(maxAge, checkInterval)
        }
    }

    /**
     * Returns the cached value for the given [key],
     * or `null` if no entry is present.
     */
    fun get(key: K): T? = data[key]?.value

    /**
     * Returns the cached value for [key]. If no entry is present,
     * [loader] is called, the result is stored and returned.
     *
     * Note: Under concurrent access, [loader] may be called more than once in rare cases.
     * The stored value remains consistent.
     *
     * @param key Key of the entry to look up.
     * @param loader Suspend function that computes or loads the value if
     *   no cache hit is found.
     */
    suspend fun getOrSet(key: K, loader: suspend () -> T): T {
        data[key]?.let { return it.value }
        val value = loader()
        return data.putIfAbsent(key, CacheEntry(value))?.value ?: value
    }

    /**
     * Stores [value] under the given [key].
     * An existing entry will be overwritten.
     */
    fun set(key: K, value: T) {
        data[key] = CacheEntry(value)
    }

    /**
     * Removes the entry with the given [key] from the cache,
     * invokes [onEvict], and returns the associated value,
     * or `null` if no entry was present.
     */
    fun remove(key: K): T? = data.remove(key)?.value?.also { onEvict?.invoke(key, it) }

    /**
     * Removes all entries from the cache and invokes [onEvict] for each.
     */
    fun clear() {
        if (onEvict != null) {
            data.forEach { (key, entry) -> onEvict.invoke(key, entry.value) }
        }
        data.clear()
    }

    /**
     * Removes all entries older than [maxAge] and invokes [onEvict] for each.
     * If the cache is empty, the iteration is skipped.
     *
     * Can be called manually at any time, regardless of whether
     * automatic cleanup is active or not.
     *
     * @param maxAge Maximum age of an entry. Older entries will be deleted.
     *   Defaults to [Cache.maxAge] if provided at construction.
     */
    fun cleanup(maxAge: Duration = this.maxAge ?: error("No maxAge specified.")) {
        if (isEmpty) return
        val cutoff = System.currentTimeMillis() - maxAge.inWholeMilliseconds
        data.entries.removeIf { (key, entry) ->
            (entry.timestamp < cutoff).also { evicted ->
                if (evicted) onEvict?.invoke(key, entry.value)
            }
        }
    }

    /**
     * Starts automatic cleanup.
     *
     * The job checks every [checkInterval] whether entries older than [maxAge] are present
     * and removes them. Any already running cleanup job is stopped and restarted.
     *
     * @param maxAge Maximum age of an entry. Older entries will be deleted.
     *   Defaults to [Cache.maxAge] if provided at construction.
     * @param checkInterval Time interval between two cleanup runs.
     *   Defaults to [maxAge].
     */
    fun startAutoCleanup(
        maxAge: Duration = this.maxAge ?: error("No maxAge specified."),
        checkInterval: Duration = maxAge
    ) {
        cleanupJob?.cancel()
        cleanupJob = scope.launch {
            while (isActive) {
                delay(checkInterval)
                cleanup(maxAge)
            }
        }
    }

    /**
     * Stops automatic cleanup if it is active.
     * Already stored entries are not affected.
     */
    fun stopAutoCleanup() {
        cleanupJob?.cancel()
        cleanupJob = null
    }
}
