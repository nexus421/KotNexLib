package kotnexlib

import ResultOf2
import java.io.File

object DocxHelper {

    /**
     * bash -l -c <command> ist notwendig, damit Java das zip/unzip korrekt ausführen kann.
     * Beim erneuten zippen, müssen wir im tempOrdner sein, da sonst der Zusammenbau der Zip nicht richtig funktioniert!
     *
     * @param baseDocumentFile Quelldatei. Diese docx wollen wir manipulieren
     * @param tempFolder Hierhin entpacken wir das [baseDocumentFile] Dokument.
     * @param docxDestinationFile Hier speichern wir die veränderte docx ab.
     */
    fun manipulateDocxCommandLine(baseDocumentFile: File, tempFolder: File, docxDestinationFile: File): Boolean {
        tempFolder.deleteRecursively() //Temp darf nicht mehr da sein.


        //docx unzippen
        val processBuilderUnzip =
            ProcessBuilder("bash", "-l", "-c", "unzip ${baseDocumentFile.absolutePath} -d ${tempFolder.absolutePath}")
        processBuilderUnzip.start().apply {
            val code = waitFor()
            val executionSucceeded = code == 0
            if (executionSucceeded.not()) return false
        }


        //In document.xml steht der ganze Text. Hierdrin dann suchen und ersetzen
        val documentXML = File(tempFolder.absolutePath + "${File.separator}word${File.separator}document.xml")
        val text = documentXML.readText()
        //ToDo: Hier den Text suchen und ersetzen. Wir suchen alle Platzhalter >> ${...} <<. Suchen dafür dann die tatsächlichen Werte aus dem Auftrag. Setzen diese hier ein. Speichern. Zurück in docx.
        val changed = text.replace("Ersatz", "Kadoffesalat", true)
        documentXML.writeText(changed)


        //Muss genau so ablaufen!
        val processBuilder = ProcessBuilder("bash", "-l", "-c", "zip -r ${docxDestinationFile.absolutePath} *")
        processBuilder.directory(tempFolder.absoluteFile)
        processBuilder.start().apply {
            val code = waitFor()
            val executionSucceeded = code == 0
            if (executionSucceeded.not()) return false
        }
        return true
    }

    /**
     * Unzips an docx using thr command line tool "unzip".
     * "unzip" needs to be installed and will not be checked vor availability
     */
    fun unzipFile(docxFile: File, destinationFolder: File): ResultOf2<Unit, Int> {
        val processBuilderUnzip =
            ProcessBuilder("bash", "-l", "-c", "unzip ${docxFile.absolutePath} -d ${destinationFolder.absolutePath}")
        processBuilderUnzip.start().apply {
            val code = waitFor()
            val executionSucceeded = code == 0
            if (executionSucceeded.not()) return ResultOf2.Failure(code)
        }

        return ResultOf2.Success(Unit)
    }

    /**
     * Zips an previous extracted docx back to a docx, This is done through the comandline tool "zip".
     *
     * @param docxDestinationFile destination docx file to store the compression result
     * @param folderToZip the folder where the docx previously was extracted. We use this as working directory.
     */
    fun zipFile(docxDestinationFile: File, folderToZip: File): ResultOf2<Unit, Int> {
        val processBuilder = ProcessBuilder("bash", "-l", "-c", "zip -r ${docxDestinationFile.absolutePath} *")
        processBuilder.directory(folderToZip.absoluteFile)
        processBuilder.start().apply {
            val code = waitFor()
            val executionSucceeded = code == 0
            if (executionSucceeded.not()) return false
        }
    }


    /**
     * Wir gehen davon aus, das LibreOffice installiert ist
     *
     * @param documentToConvert Datei, die in eine PDF konvertiert werden soll.
     * @param pdfDestinationFolder Zielordner, in den die PDF gespeichert werden soll. Der Name ist nicht änderbar und lautet so wie der Name der docx nur mit .pdf als Endung! (Default auf gleichen Ordner wie die docx)
     */
    fun createPdfWithLibreOffice(
        documentToConvert: File,
        pdfDestinationFolder: File = documentToConvert.parentFile
    ): Boolean {
        val processBuilder = ProcessBuilder(
            "bash",
            "-l",
            "-c",
            "libreoffice --headless --convert-to pdf ${documentToConvert.absolutePath} --outdir ${pdfDestinationFolder.absolutePath}"
        )
        processBuilder.start().apply {
            val code = waitFor()
            val executionSucceeded = code == 0
            if (executionSucceeded.not()) return false
        }


        return true
    }

}