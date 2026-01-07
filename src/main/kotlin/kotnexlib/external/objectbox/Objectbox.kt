package kotnexlib.external.objectbox

import io.objectbox.Box
import io.objectbox.query.Query
import io.objectbox.query.QueryCondition

fun <T> Query<T>.findAndClose(maxLogs: Int? = null): List<T> =
    use { if (maxLogs == null) it.find() else it.find(0, (maxLogs).toLong()) }

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

/**
 * Checks if the Box contains an item that satisfies the given query condition.
 * The used query will be closed afterward.
 *
 * @param queryCondition the query condition to check against the items in the Box
 * @return true if the Box contains an item that satisfies the query condition, false otherwise
 */
fun <T> Box<T>.contains(queryCondition: QueryCondition<T>): Boolean {
    return query(queryCondition).build().use {
        it.count() > 0
    }
}

/**
 * Returns the first element that matches the given query condition, or null if no elements match.
 * The used query will be closed afterward.
 *
 * @param queryCondition the query condition to match elements against
 * @return the first matching element, or null if no elements match
 */
fun <T> Box<T>.findOrNull(queryCondition: QueryCondition<T>): T? = query(queryCondition).build().findFirstAndClose()

/**
 * Finds all elements in the box that match the given query condition.
 * The used query will be closed afterward.
 *
 * @param queryCondition The query condition to match against.
 * @return A list of elements that match the query condition.
 */
fun <T> Box<T>.findAll(queryCondition: QueryCondition<T>): List<T> = query(queryCondition).build().findAndClose()