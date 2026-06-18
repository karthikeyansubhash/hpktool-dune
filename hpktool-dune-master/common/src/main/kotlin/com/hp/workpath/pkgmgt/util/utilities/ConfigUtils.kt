package com.hp.workpath.pkgmgt.util.utilities

import java.io.File
import java.io.InputStream
import kotlin.io.path.createTempFile

object ConfigUtils {
    private const val DEFAULT_CONFIG_RESOURCE_PATH = "/configs/default.json"

    fun getDefaultConfigFile(): File? {
        return try {
            val resourceStream = getDefaultConfigStream()
            if (resourceStream != null) {
                val tempFile = createTempFile("default", ".json").toFile()
                tempFile.deleteOnExit()

                resourceStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } else {
                println("Warning: default.json not found in resources")
                null
            }
        } catch (e: Exception) {
            println("Error loading default config: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun getDefaultConfigStream(): InputStream? {
        return ConfigUtils::class.java.getResourceAsStream(DEFAULT_CONFIG_RESOURCE_PATH)
    }

}