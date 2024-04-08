package kotnexlib

import io.objectbox.query.Query

fun <T> Query<T>.findAndClose(maxLogs: Int): List<T> = use { it.find(0, (maxLogs).toLong()) }

fun <T> Query<T>.findFirstAndClose(): T? = use { it.findFirst() }

fun <T> Query<T>.removeAndClose(): Long = use { it.remove() }

/**
 * Processes only [pageSize] database rows at a time.
 *
 * Meaning: We load the first [pageSize] database rows and pass them to [onEachPage]. Then we load the next [pageSize] database rows. This continues until we have processed [maxLimit] database rows.
 *
 * @param onEachPage Is called for each page and is always [pageSize] in size unless it is the last iteration, then it can also be smaller.
 * @param pageSize The number of database rows that should be loaded from the database at the same time.
 * @param maxLimit Specifies the maximum number of database rows to process in total. Defaults to [Query.count], so all elements will be handled.
 * @param closeAtTheEnd If false (default), the query will not be closed at the end. Otherwise, the query will be closed.
 */
fun <T> Query<T>.doForEachPages(
    onEachPage: (List<T>) -> Unit,
    pageSize: Long,
    maxLimit: Long = count(),
    closeAtTheEnd: Boolean = false
) {
    var offset = 0L
    var currentLimit = pageSize
    while (true) {
        println("Check logs from $offset to $currentLimit")
        val logs = find(offset, currentLimit)
        onEachPage(logs)

        offset = currentLimit
        currentLimit += pageSize
        if (offset >= maxLimit) break
    }

    if (closeAtTheEnd) close()
}

