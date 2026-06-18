package com.hp.workpath.pkgmgt.util.utilities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class BundleExtractorCommandBuilderUnitTest {
    @Test
    fun `buildTarCommand on Windows uses wsl exec and preserves special chars in path`() {
        val tarPath = "/mnt/c/Users/JediAdmin/Downloads/HPK2ToolGui-win(1)/HPK2Tool/temp/dump/Accessory%20Agent%20Sample/solution.tar.gz"
        val outPath = "/mnt/c/Users/JediAdmin/Downloads/HPK2ToolGui-win (1)/HPK2Tool/temp/squash"

        val command = BundleExtractorCommandBuilder.buildTarCommand(
            osType = OsType.Windows,
            tarPath = tarPath,
            outputPath = outPath,
        )

        assertEquals(
            listOf(
                BUNDLE_EXTRACTOR_WSL,
                BUNDLE_EXTRACTOR_WSL_EXEC,
                SPU_CMD_TAR,
                SPU_TAR_FLAG_ZXVF,
                tarPath,
                SPU_TAR_FLAG_C,
                outPath,
            ),
            command,
        )
        assertFalse(command.any { it.contains("'") })
    }

    @Test
    fun `buildTarCommand on Linux does not prepend wsl`() {
        val tarPath = "/tmp/in/solution.tar.gz"
        val outPath = "/tmp/out"

        val command = BundleExtractorCommandBuilder.buildTarCommand(
            osType = OsType.Linux,
            tarPath = tarPath,
            outputPath = outPath,
        )

        assertEquals(
            listOf(
                SPU_CMD_TAR,
                SPU_TAR_FLAG_ZXVF,
                tarPath,
                SPU_TAR_FLAG_C,
                outPath,
            ),
            command,
        )
    }

    @Test
    fun `buildUnsquashCommand on Windows uses wsl exec`() {
        val outputPath = "/mnt/c/Users/JediAdmin/Downloads/HPK2ToolGui-win(2)/HPK2Tool/temp/stage"
        val inputPath = "/mnt/c/Users/JediAdmin/Downloads/HPK2ToolGui-win(2)/HPK2Tool/temp/squash/solution.squash"

        val command = BundleExtractorCommandBuilder.buildUnsquashCommand(
            osType = OsType.Windows,
            outputPath = outputPath,
            inputPath = inputPath,
        )

        assertEquals(
            listOf(
                BUNDLE_EXTRACTOR_WSL,
                BUNDLE_EXTRACTOR_WSL_EXEC,
                "unsquashfs",
                "-f",
                "-d",
                outputPath,
                inputPath,
            ),
            command,
        )
    }
}

