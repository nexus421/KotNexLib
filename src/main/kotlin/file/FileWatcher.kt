package file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds

/** Kind of filesystem change, see [FileChangeEvent]. */
enum class FileChangeKind { CREATED, MODIFIED, DELETED }

/**
 * @property file the affected file.
 * @property kind the kind of change.
 */
data class FileChangeEvent(val file: File, val kind: FileChangeKind)

/**
 * Watches this directory reactively via the Java NIO `WatchService`, exposed as a [Flow].
 *
 * Runs on a dedicated background thread rather than directly in the coroutine dispatcher:
 * `WatchService.take()` blocks and does not react to coroutine cancellation, only to a new event or
 * to the `WatchService` being closed — the latter happens when the flow is closed, via `awaitClose`.
 *
 * @return a [Flow] that emits a [FileChangeEvent] for every filesystem change in this directory.
 * @throws java.io.IOException if this directory does not exist or the watch service cannot be created.
 */
fun File.watchDirectory(): Flow<FileChangeEvent> = callbackFlow {
    val path = toPath()
    val watchService = FileSystems.getDefault().newWatchService()
    val watchKey = try {
        path.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE
        )
    } catch (e: Exception) {
        // Registration failed (e.g. this directory does not exist) - close before propagating, or the
        // watch service's underlying OS resource (e.g. an inotify file descriptor) leaks.
        watchService.close()
        throw e
    }

    val runnerThread = Thread {
        // awaitClose() below stops this loop by both closing the watch service and interrupting this
        // thread, so either a ClosedWatchServiceException or an InterruptedException from take() is the
        // expected shutdown path - not a real failure. Anything else is unexpected and must not be
        // silently swallowed, otherwise the flow would hang forever without ever completing or failing.
        runCatching {
            while (isClosedForSend.not()) {
                val key = watchService.take()
                for (event in key.pollEvents()) {
                    val kind = when (event.kind()) {
                        StandardWatchEventKinds.ENTRY_CREATE -> FileChangeKind.CREATED
                        StandardWatchEventKinds.ENTRY_MODIFY -> FileChangeKind.MODIFIED
                        StandardWatchEventKinds.ENTRY_DELETE -> FileChangeKind.DELETED
                        else -> continue
                    }
                    val changedFile = path.resolve(event.context() as Path).toFile()
                    trySend(FileChangeEvent(changedFile, kind))
                }
                val keyStillValid = key.reset()
                if (keyStillValid.not()) break
            }
        }.onFailure { error ->
            if (error !is ClosedWatchServiceException && error !is InterruptedException) close(error)
        }
        close()
    }.apply { isDaemon = true; start() }

    awaitClose {
        watchKey.cancel()
        watchService.close()
        runnerThread.interrupt()
    }
}.flowOn(Dispatchers.IO)
