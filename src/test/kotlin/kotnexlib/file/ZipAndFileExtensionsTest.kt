package kotnexlib.file

import file.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ZipAndFileExtensionsTest {

    @Test
    fun testFileExistsDirAndExistsFile(@TempDir tempDir: File) {
        val testDir = File(tempDir, "sub/deep/folder")
        assertFalse(testDir.exists())

        assertTrue(testDir.existsDir(createIfNotExist = true))
        assertTrue(testDir.exists())
        assertTrue(testDir.isDirectory)

        val testFile = File(tempDir, "test.txt")
        assertFalse(testFile.exists())
        assertTrue(testFile.existsFile(createIfNotExist = true))
        assertTrue(testFile.exists())
        assertTrue(testFile.isFile)
    }

    @Test
    fun testZipAndUnzipDirectory(@TempDir tempDir: File) {
        val sourceDir = File(tempDir, "source").apply { mkdirs() }
        val file1 = File(sourceDir, "file1.txt").apply { writeText("Hello World from file 1") }
        val subDir = File(sourceDir, "subdir").apply { mkdirs() }
        val file2 = File(subDir, "file2.txt").apply { writeText("Nested content") }

        val zipRes = sourceDir.zipFiles(parentFolderToStoreZip = tempDir, zipName = "bundle.zip")
        assertTrue(zipRes is ZipResult.Success)
        val zipFile = (zipRes as ZipResult.Success).result
        assertTrue(zipFile.exists())
        assertTrue(zipFile.isZipFile())

        val targetDir = File(tempDir, "unzipped")
        val unzipRes = zipFile.unzipFile(destinationFolder = targetDir)
        assertTrue(unzipRes is UnzipResult.Success)

        val extractedFile1 = File(targetDir, "file1.txt")
        assertTrue(extractedFile1.exists())
        assertEquals("Hello World from file 1", extractedFile1.readText())

        val extractedFile2 = File(targetDir, "subdir/file2.txt")
        assertTrue(extractedFile2.exists())
        assertEquals("Nested content", extractedFile2.readText())
    }

    @Test
    fun testUseAndDelete(@TempDir tempDir: File) {
        val tempFile = File(tempDir, "temp_data.txt").apply { writeText("temporary") }
        assertTrue(tempFile.exists())

        val readResult = tempFile.useAndDelete {
            readText()
        }

        assertEquals("temporary", readResult)
        assertFalse(tempFile.exists())
    }

    @Test
    fun testZipFilesDoesNotLeaveFileBehindOnEmptyFolder(@TempDir tempDir: File) {
        val emptySourceDir = File(tempDir, "empty_source").apply { mkdirs() }

        val zipRes = emptySourceDir.zipFiles(parentFolderToStoreZip = tempDir, zipName = "should_not_exist.zip")
        assertTrue(zipRes is ZipResult.FolderIsEmpty)

        // Regression test: an empty/failed zip attempt must not leave an invalid zip file on disk.
        assertFalse(File(tempDir, "should_not_exist.zip").exists())
    }

    @Test
    fun testZipSlipVulnerabilityPrevention(@TempDir tempDir: File) {
        val maliciousZip = File(tempDir, "malicious.zip")
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(maliciousZip)).use { zipOut ->
            // Entry attempting to escape target directory
            val entry = java.util.zip.ZipEntry("../../escaped.txt")
            zipOut.putNextEntry(entry)
            zipOut.write("malicious payload".toByteArray())
            zipOut.closeEntry()
        }

        val targetDir = File(tempDir, "safe_target").apply { mkdirs() }
        val unzipRes = maliciousZip.unzipFile(destinationFolder = targetDir)

        // Must fail with Failure result describing security violation
        assertTrue(unzipRes is UnzipResult.Failure)
        val failureMsg = (unzipRes as UnzipResult.Failure).errorMessage
        assertTrue(failureMsg.contains("Zip slip"))
    }
}
