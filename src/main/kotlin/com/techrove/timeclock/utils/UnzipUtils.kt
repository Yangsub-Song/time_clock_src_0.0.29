package com.techrove.timeclock.utils

import mu.KotlinLogging
import okhttp3.internal.closeQuietly
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.*
import java.nio.charset.Charset
import java.util.zip.ZipFile

private val logger = KotlinLogging.logger("Unzip")

/**
 * UnzipUtils class extracts files and sub-directories of a standard zip file to
 * a destination directory.
 *
 */
object UnzipUtils {
    /**
     * @param zipFilePath
     * @param destinationFolder
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destinationFolder: String) {
        File(destinationFolder).run {
            if (!exists()) {
                mkdirs()
            }
        }

        ZipFile(zipFilePath, Charset.forName("cp949")).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    logger.debug { "### $entry" }
                    val filePath = destinationFolder + File.separator + entry.name
                    if (!entry.isDirectory) {
                        logger.trace { "-> $filePath" }
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        dir.mkdir()
                        logger.trace { "=> $filePath" }
                    }

                }

            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     * @param inputStream
     * @param destFilePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        File(destFilePath).parentFile.let {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

//    fun File.unzipEncrypted(unzipTo: String, password: String = "ASman@8282"): Boolean {
    fun File.unzipEncrypted(unzipTo: String, password: String): Boolean {   //  Yade0924
        logger.debug { "extracting $this" }
        var sevenZFile: SevenZFile? = null
        try {
            val unzipDestination = File(unzipTo)
            if (!unzipDestination.exists()) {
                unzipDestination.mkdirs()
            }
            sevenZFile = SevenZFile(this, password.toCharArray())
            var entry = sevenZFile.nextEntry
            while (entry != null) {
                logger.debug { "### ${entry.name}" }
                if (entry.isDirectory) {
                    File(unzipDestination, entry.name).mkdirs()
                } else {
                    val content = ByteArray(entry.size.toInt())
                    sevenZFile.read(content)
                    File(unzipDestination, entry.name).writeBytes(content)
                }
                entry = sevenZFile.nextEntry
            }
            logger.debug { "extracting $this success" }
            return true
        } catch (e: Exception) {
            logger.error { e }
            logger.error { "extracting $this failed" }
            return false
        } finally {
            sevenZFile?.closeQuietly()
        }
    }

    /**
     * Size of the buffer to read/write data
     */
    private const val BUFFER_SIZE = 4096
}

