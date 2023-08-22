package org.escalaralcoiaicomtat.android.utils

import java.io.File

object FileUtils {
    /**
     * Calculates the size of all the files inside a directory in bytes.
     */
    fun File.dirSize(): Long =
        if (exists()) {
            listFiles()?.sumOf { file ->
                if (file.isDirectory) {
                    // Recursive call if it's a directory
                    file.dirSize()
                } else {
                    // Sum the file size in bytes
                    file.length()
                }
            } ?: 0L
        } else {
            0L
        }
}
