package com.hp.workpath.pkgmgt.util.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.ext.types.solutionManager.SolutionManifest
import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.utilities.apk.ApkParser
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

class BundleExtractor(bundleFile: File) {
    val apkParser: ApkParser
    val solutionProject: SolutionProject

    init {
        try {
            // create temp directory
            val tempDirPath = Path.of(Paths.get("").absolutePathString(), DIR_TEMP)
            tempDirPath.toFile().mkdirs()
            // extract tar.gz from bdl
            var bundleSerialNumber: String
            SpuExecutor(tempDirPath.toString()).apply {
                val fimDumpResult = executeSpuFimDump(bundleFile.toPath().toAbsolutePath())
                bundleSerialNumber = fimDumpResult.substringAfter(SPU_DUMP_RESULT_SERIAL_NUMBER).substringBefore("\r\n")
            }
            val tarFiles =
                findFilesWithLastString(Path.of(tempDirPath.toString(), DIRECTORY_DUMP), TAR_GZ_EXTENSION_WITH_DOT)
            if (tarFiles.size != 1) {
                throw Exception("Invalid number of tar.gz files found: ${tarFiles.size}")
            }
            // extract squash and apk from tar.gz
            Path.of(tempDirPath.toString(), DIRECTORY_SQUASH).toFile().mkdirs()
            extractTar(tarFiles.first(), Path.of(tempDirPath.toString(), DIRECTORY_SQUASH))
            val apkFiles =
                findFilesWithLastString(Path.of(tempDirPath.toString(), DIRECTORY_SQUASH), APK_EXTENSION_WITH_DOT)
            if (apkFiles.size != 1) {
                throw Exception("Invalid number of apk files found: ${apkFiles.size}")
            }
            val apkFile = apkFiles.first()
            apkParser = ApkParser(apkFile)
            val squashFiles =
                findFilesWithLastString(Path.of(tempDirPath.toString(), DIRECTORY_SQUASH), SQUASH_EXTENSION_WITH_DOT)
            if (squashFiles.size != 1) {
                throw Exception("Invalid number of squash files found: ${squashFiles.size}")
            }
            // extract solution.json and data from squash
            Path.of(tempDirPath.toString(), DIRECTORY_STAGE).toFile().mkdirs()
            extractSquash(squashFiles.first(), Path.of(tempDirPath.toString(), DIRECTORY_STAGE))
            // build solutionProject
            val solutionManifest = ObjectMapper().readValue(
                findFile(Path.of(tempDirPath.toString(), DIRECTORY_STAGE), MANIFEST_FILE_NAME),
                SolutionManifest::class.java
            )
            solutionProject = SolutionProject().apply {
                from(
                    solutionManifest,
                    Path.of(Paths.get("").absolutePathString(), DIR_TEMP, DIRECTORY_STAGE).toAbsolutePath()
                )
            }
            solutionProject.apkPath = apkFile.toPath().toAbsolutePath()
            solutionProject.solutionBundleSerial = bundleSerialNumber
        } catch (exception: Exception) {
            throw exception
        }
    }

    private fun extractTar(input: File, outputDir: Path): String {
        val osType = getOsType()
        val tarPath = windowsPathToWslPath(input.toPath().toAbsolutePath().toString())
        val outPath = windowsPathToWslPath(outputDir.toAbsolutePath().toString())

        val command = BundleExtractorCommandBuilder.buildTarCommand(
            osType = osType,
            tarPath = tarPath,
            outputPath = outPath,
        )
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val finished = process.waitFor(EXTERNAL_PROCESS_TIMEOUT, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            throw Exception("Tar extraction process timed out.")
        }
        val output = process.inputStream.bufferedReader().use { it.readText() }
        if (process.exitValue() != 0) {
            throw Exception(output)
        }
        return output
    }

    private fun extractSquash(input: File, outputDir: Path): String {
        val osType = getOsType()
        val outputPath = windowsPathToWslPath(outputDir.toAbsolutePath().toString())
        val inputPath = windowsPathToWslPath(input.toPath().toAbsolutePath().toString())

        val command = BundleExtractorCommandBuilder.buildUnsquashCommand(
            osType = osType,
            outputPath = outputPath,
            inputPath = inputPath,
        )
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val finished = process.waitFor(EXTERNAL_PROCESS_TIMEOUT, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            throw Exception("Squash extraction process timed out.")
        }
        val output = process.inputStream.bufferedReader().use { it.readText() }
        if (process.exitValue() != 0) {
            throw Exception(output)
        }
        return output
    }


    private fun findFilesWithLastString(directory: Path, extension: String): List<File> {
        val files = mutableListOf<File>()
        val dir = directory.toFile()
        val children = dir.listFiles()

        if (children != null) {
            for (child in children) {
                if (child.isFile && child.name.endsWith(extension, true)) {
                    files.add(child)
                }

                if (child.isDirectory) {
                    files.addAll(findFilesWithLastString(Path.of(child.path), extension))
                }
            }
        }
        return files
    }

    private fun findFile(directory: Path, name: String): File? {
        val dir = directory.toFile()
        val children = dir.listFiles()
        if (children != null) {
            for (child in children) {
                if (child.isFile && child.name.equals(name, true)) {
                    return child
                }
                if (child.isDirectory) {
                    val file = findFile(Path.of(child.path), name)
                    if (file != null) {
                        return file
                    }
                }
            }
        }
        return null
    }
}

internal object BundleExtractorCommandBuilder {
    fun buildTarCommand(osType: OsType, tarPath: String, outputPath: String): MutableList<String> {
        val command = mutableListOf(
            SPU_CMD_TAR,
            SPU_TAR_FLAG_ZXVF,
            tarPath,
            SPU_TAR_FLAG_C,
            outputPath,
        )
        if (osType == OsType.Windows) {
            // --exec bypasses bash shell so path chars like spaces and parentheses are literal args.
            command.add(0, BUNDLE_EXTRACTOR_WSL_EXEC)
            command.add(0, BUNDLE_EXTRACTOR_WSL)
        }
        return command
    }

    fun buildUnsquashCommand(osType: OsType, outputPath: String, inputPath: String): MutableList<String> {
        val command = mutableListOf(
            "unsquashfs",
            "-f",
            "-d",
            outputPath,
            inputPath,
        )
        if (osType == OsType.Windows) {
            // --exec bypasses bash shell so path chars like spaces and parentheses are literal args.
            command.add(0, BUNDLE_EXTRACTOR_WSL_EXEC)
            command.add(0, BUNDLE_EXTRACTOR_WSL)
        }
        return command
    }
}
