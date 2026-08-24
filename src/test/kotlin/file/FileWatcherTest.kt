package file

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException

class FileWatcherTest {

    @Test
    fun testDetectsFileCreation(@TempDir tempDir: File) = runBlocking {
        val eventDeferred = async {
            withTimeout(10_000) {
                tempDir.watchDirectory().first { it.kind == FileChangeKind.CREATED }
            }
        }
        delay(300) // give the watch service time to register before triggering the change
        val newFile = File(tempDir, "created.txt").apply { writeText("hello") }

        val event = eventDeferred.await()
        assertEquals(FileChangeKind.CREATED, event.kind)
        assertEquals(newFile.name, event.file.name)
    }

    @Test
    fun testDetectsFileModification(@TempDir tempDir: File) = runBlocking {
        val existingFile = File(tempDir, "existing.txt").apply { writeText("initial") }

        val eventDeferred = async {
            withTimeout(10_000) {
                tempDir.watchDirectory().first { it.kind == FileChangeKind.MODIFIED }
            }
        }
        delay(300)
        existingFile.appendText(" changed")

        val event = eventDeferred.await()
        assertEquals(FileChangeKind.MODIFIED, event.kind)
        assertEquals(existingFile.name, event.file.name)
    }

    @Test
    fun testDetectsFileDeletion(@TempDir tempDir: File) = runBlocking {
        val existingFile = File(tempDir, "toDelete.txt").apply { writeText("bye") }

        val eventDeferred = async {
            withTimeout(10_000) {
                tempDir.watchDirectory().first { it.kind == FileChangeKind.DELETED }
            }
        }
        delay(300)
        existingFile.delete()

        val event = eventDeferred.await()
        assertEquals(FileChangeKind.DELETED, event.kind)
        assertEquals(existingFile.name, event.file.name)
    }

    @Test
    fun testWatchingNonExistentDirectoryFailsFlowInsteadOfHanging(@TempDir tempDir: File) {
        val nonExistentDir = File(tempDir, "does-not-exist")
        assertThrows(IOException::class.java) {
            runBlocking {
                withTimeout(5_000) {
                    nonExistentDir.watchDirectory().first()
                }
            }
        }
    }
}
