package com.hp.workpath.pkgmgt.util.utilities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class SpuExecutableResolverUnitTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `resolve prefers direct executable override for absolute CI path`() {
        val repoExecutable = createFile(tempDir.resolve("repo/common/spu_windows/spu-cli.exe"))
        val explicitExecutable = createFile(tempDir.resolve("ci/tools/spu-cli.exe"))

        val resolved = SpuExecutableResolver.resolve(
            fileName = "spu-cli.exe",
            userDir = tempDir.resolve("repo/gui"),
            codeSourcePath = null,
            systemPropertyProvider = { key ->
                if (key == SPU_PROPERTY_EXECUTABLE) explicitExecutable.toString() else null
            },
            environmentProvider = { null },
        )

        assertEquals(explicitExecutable.toAbsolutePath().normalize(), resolved)
        assertTrue(Files.exists(repoExecutable))
    }

    @Test
    fun `resolve supports direct spu home override for common spu directory`() {
        val spuHome = tempDir.resolve("ci/common/spu_windows")
        val executable = createFile(spuHome.resolve("spu-cli.exe"))

        val resolved = SpuExecutableResolver.resolve(
            fileName = "spu-cli.exe",
            userDir = tempDir.resolve("repo/gui"),
            codeSourcePath = null,
            systemPropertyProvider = { key ->
                if (key == SPU_PROPERTY_HOME) spuHome.toString() else null
            },
            environmentProvider = { null },
        )

        assertEquals(executable.toAbsolutePath().normalize(), resolved)
    }

    @Test
    fun `resolve supports legacy home override for repo root`() {
        val executable = createFile(tempDir.resolve("workspace/common/spu_windows/spu-cli.exe"))

        val resolved = SpuExecutableResolver.resolve(
            fileName = "spu-cli.exe",
            userDir = tempDir.resolve("workspace/gui"),
            codeSourcePath = null,
            systemPropertyProvider = { key ->
                if (key == HPKTOOL_PROPERTY_HOME_LEGACY) tempDir.resolve("workspace").toString() else null
            },
            environmentProvider = { null },
        )

        assertEquals(executable.toAbsolutePath().normalize(), resolved)
    }

    @Test
    fun `resolve falls back to walking up from submodule working directory`() {
        val executable = createFile(tempDir.resolve("project/common/spu_windows/spu-cli.exe"))

        val resolved = SpuExecutableResolver.resolve(
            fileName = "spu-cli.exe",
            userDir = tempDir.resolve("project/gui/build"),
            codeSourcePath = null,
            systemPropertyProvider = { null },
            environmentProvider = { null },
        )

        assertEquals(executable.toAbsolutePath().normalize(), resolved)
    }

    @Test
    fun `resolve failure includes absolute override path for debugging`() {
        val missingExecutable = tempDir.resolve("ci/tools/spu-cli.exe").toAbsolutePath().normalize()

        val exception = org.junit.jupiter.api.Assertions.assertThrows(Exception::class.java) {
            SpuExecutableResolver.resolve(
                fileName = "spu-cli.exe",
                userDir = tempDir.resolve("repo/gui"),
                codeSourcePath = null,
                systemPropertyProvider = { key ->
                    if (key == SPU_PROPERTY_EXECUTABLE) missingExecutable.toString() else null
                },
                environmentProvider = { null },
            )
        }

        assertTrue(exception.message!!.contains(missingExecutable.toString()))
        assertTrue(exception.message!!.contains("Tried:"))
    }

    private fun createFile(path: Path): Path {
        Files.createDirectories(path.parent)
        return Files.createFile(path)
    }
}
