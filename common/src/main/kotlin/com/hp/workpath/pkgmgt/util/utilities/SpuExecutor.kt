package com.hp.workpath.pkgmgt.util.utilities

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists

class SpuExecutor(private val outputPathString: String) {
    fun executeSpuSquash(): String {
        Files.createDirectories(Paths.get(outputPathString, DIRECTORY_SQUASH))
        val options = listOf(
            SPU_CMD_SQUASHFS,
            SPU_OPT_INPUT,
            windowsPathToWslPath(Path.of(outputPathString, DIRECTORY_STAGE).toString()),
            SPU_OPT_OUTPUT,
            windowsPathToWslPath(
                Path.of(outputPathString, DIRECTORY_SQUASH, FILENAME_SQUASH).toString()
            )
        )
        return executeSpu(args = options.map { it }.toTypedArray())
    }

    fun executeSpuTar(): String {
        Files.createDirectories(Paths.get(outputPathString, DIRECTORY_TAR))
        val options = listOf(
            SPU_CMD_TAR,
            SPU_OPT_INPUT,
            windowsPathToWslPath(Path.of(outputPathString, DIRECTORY_SQUASH).toString()),
            SPU_OPT_OUTPUT,
            windowsPathToWslPath(Path.of(outputPathString, DIRECTORY_TAR, FILENAME_TAR).toString())
        )
        return executeSpu(args = options.map { it }.toTypedArray())
    }

    fun executeSpuSignTar(signingKeyPath: Path): String {
        val options = listOf(
            SPU_CMD_SIGN_TAR,
            SPU_OPT_INPUT,
            windowsPathToWslPath(Path.of(outputPathString, DIRECTORY_TAR, FILENAME_TAR).toString()),
            SPU_OPT_OUTPUT,
            windowsPathToWslPath(
                Path.of(outputPathString, DIRECTORY_TAR, FILENAME_TAR_GZ_SIG).toString()
            ),
            SPU_OPT_KEY,
            windowsPathToWslPath(signingKeyPath.toString())
        )
        return executeSpu(args = options.map { it }.toTypedArray())
    }

    fun executeSpuFimPak(vendor: String, name: String, description: String, version: String): String {
        Files.createDirectories(Paths.get(outputPathString, DIRECTORY_PAK))
        val options = listOf(
            SPU_CMD_FIM_PAK,
            SPU_OPT_FOLDER,
            Path.of(outputPathString, DIRECTORY_TAR).toString(),
            SPU_OPT_OUTPUT,
            Path.of(outputPathString, DIRECTORY_PAK, FILENAME_PAK).toString(),
            SPU_OPT_VENDOR,
            vendor,
            SPU_OPT_NAME,
            name,
            SPU_OPT_DESCRIPTION,
            description,
            SPU_OPT_VERSION,
            version
        )
        return executeSpu(args = options.map { it }.toTypedArray())
    }

    fun executeSpuFimBdl(
        bundleName: String,
        vendor: String,
        name: String,
        description: String,
        version: String,
        solutionId: String,
        solutionBundleSerial: String,
        supportEmail: String?,
        supportPhone: String?,
        supportUrl: String?,
    ): String {
        val options = mutableListOf(
            SPU_CMD_FIM_BDL,
            SPU_OPT_INPUT,
            Path.of(outputPathString, DIRECTORY_PAK, FILENAME_PAK).toString(),
            SPU_OPT_OUTPUT,
            Path.of(outputPathString, bundleName + HPK2_EXTENSION_WITH_DOT).toString(),
            SPU_OPT_VENDOR,
            vendor,
            SPU_OPT_NAME,
            name,
            SPU_OPT_DESCRIPTION,
            description,
            SPU_OPT_VERSION,
            version,
            SPU_OPT_ID,
            solutionId,
            SPU_OPT_TYPE,
            SPU_TYPE_UNVERIFIED
        ).also {
            if (solutionBundleSerial.isNotEmpty()) {
                it.add(SPU_OPT_SERIAL)
                it.add(solutionBundleSerial)
            }
            if (supportEmail?.isNotEmpty() == true) {
                it.add(SPU_OPT_EMAIL)
                it.add(supportEmail)
            }
            if (supportPhone?.isNotEmpty() == true) {
                it.add(SPU_OPT_PHONE)
                it.add(supportPhone)
            }
            if (supportUrl?.isNotEmpty() == true) {
                it.add(SPU_OPT_URL)
                it.add(supportUrl)
            }
        }
        return executeSpu(args = options.map { it }.toTypedArray())
    }

    fun executeSpuFimDump(bundlePath: Path): String {
        val options = listOf(
            SPU_CMD_FIM_DUMP,
            SPU_OPT_INPUT,
            bundlePath.toString(),
            SPU_OPT_OUTPUT,
            Path.of(outputPathString, DIRECTORY_DUMP).toString(),
            SPU_OPT_EXTRACT
        )
        return executeSpu(args = options.map { it }.toTypedArray())
    }

    private fun executeSpu(vararg args: String): String {
        val process = ProcessBuilder(*getSpuExecutable(), *args).start().also { it.waitFor(EXTERNAL_PROCESS_TIMEOUT, TimeUnit.SECONDS) }
        if (process.exitValue() != 0) {
            throw Exception(process.errorStream.bufferedReader().readText())
        }
        val resultText = process.inputStream.bufferedReader().readText()
        // when some spu-cli components are missing
        if (resultText.contains("Could not load file or assembly")) {
            throw Exception(resultText)
        }
        return resultText
    }

    private fun getSpuExecutable(): Array<String> {
        val userDir = Paths.get(System.getProperty(SYSTEM_PROPERTY_USER_DIR)).toAbsolutePath().normalize()
        val codeSourcePath = runCatching {
            Path.of(SpuExecutor::class.java.protectionDomain.codeSource.location.toURI())
        }.getOrNull()

        return when (getOsType()) {
            OsType.Windows -> arrayOf(
                SpuExecutableResolver.resolve(
                    fileName = SPU_EXECUTABLE_WINDOWS,
                    userDir = userDir,
                    codeSourcePath = codeSourcePath,
                    systemPropertyProvider = System::getProperty,
                    environmentProvider = System::getenv,
                ).toString()
            )
            OsType.Linux -> arrayOf(
                SPU_RUNTIME_DOTNET,
                SpuExecutableResolver.resolve(
                    fileName = SPU_EXECUTABLE_LINUX_DLL,
                    userDir = userDir,
                    codeSourcePath = codeSourcePath,
                    systemPropertyProvider = System::getProperty,
                    environmentProvider = System::getenv,
                ).toString()
            )
            else -> throw Exception(EXCEPTION_OPERATING_SYSTEM_NOT_SUPPORTED)
        }
    }
}

internal object SpuExecutableResolver {
    private val executablePropertyKeys = listOf(SPU_PROPERTY_EXECUTABLE)
    private val executableEnvKeys = listOf(SPU_ENV_EXECUTABLE)
    private val homePropertyKeys = listOf(SPU_PROPERTY_HOME, HPKTOOL_PROPERTY_HOME_LEGACY)
    private val homeEnvKeys = listOf(SPU_ENV_HOME, HPKTOOL_ENV_HOME_LEGACY)

    fun resolve(
        fileName: String,
        userDir: Path,
        codeSourcePath: Path?,
        systemPropertyProvider: (String) -> String?,
        environmentProvider: (String) -> String?,
    ): Path {
        val candidates = mutableListOf<Path>()

        addExecutableOverrideCandidates(candidates, fileName, executablePropertyKeys, systemPropertyProvider)
        addExecutableOverrideCandidates(candidates, fileName, executableEnvKeys, environmentProvider)
        addHomeOverrideCandidates(candidates, fileName, homePropertyKeys, systemPropertyProvider)
        addHomeOverrideCandidates(candidates, fileName, homeEnvKeys, environmentProvider)
        addTraversalCandidates(candidates, userDir, fileName)

        codeSourcePath?.let {
            addTraversalCandidates(candidates, if (Files.isRegularFile(it)) it.parent else it, fileName)
        }

        val normalizedCandidates = candidates
            .map { it.toAbsolutePath().normalize() }
            .distinct()

        return normalizedCandidates
            .firstOrNull { it.exists() }
            ?: throw Exception(
                "$EXCEPTION_CANT_FIND_SPU_EXECUTABLE. Tried:\n" +
                    normalizedCandidates.joinToString("\n")
            )
    }

    private fun addExecutableOverrideCandidates(
        candidates: MutableList<Path>,
        fileName: String,
        keys: List<String>,
        valueProvider: (String) -> String?,
    ) {
        keys.mapNotNull(valueProvider)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .forEach { overrideValue ->
                val overridePath = Paths.get(overrideValue).toAbsolutePath().normalize()
                if (overridePath.fileName?.toString()?.equals(fileName, ignoreCase = true) == true) {
                    candidates.add(overridePath)
                }
                addDirectoryCandidates(candidates, overridePath, fileName)
            }
    }

    private fun addHomeOverrideCandidates(
        candidates: MutableList<Path>,
        fileName: String,
        keys: List<String>,
        valueProvider: (String) -> String?,
    ) {
        keys.mapNotNull(valueProvider)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .forEach { overrideValue ->
                addDirectoryCandidates(candidates, Paths.get(overrideValue).toAbsolutePath().normalize(), fileName)
            }
    }

    private fun addTraversalCandidates(candidates: MutableList<Path>, startDir: Path?, fileName: String) {
        var currentDir = startDir?.toAbsolutePath()?.normalize()
        while (currentDir != null) {
            candidates.add(currentDir.resolve(SPU_DIRECTORY_COMMON).resolve(SPU_DIRECTORY_WINDOWS).resolve(fileName))
            currentDir = currentDir.parent
        }
    }

    private fun addDirectoryCandidates(candidates: MutableList<Path>, basePath: Path, fileName: String) {
        candidates.add(basePath.resolve(fileName))
        candidates.add(basePath.resolve(SPU_DIRECTORY_COMMON).resolve(SPU_DIRECTORY_WINDOWS).resolve(fileName))
    }
}