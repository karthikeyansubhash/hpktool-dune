package com.hp.workpath.pkgmgt.util.utilities.attestation

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Low-level utility for ADB operations
 */
object AttestationManager {
    private const val WIN_RUNTIME = "cmd.exe /C "
    private const val LINUX_RUNTIME = "/bin/bash -l -c "

    private fun isWindows(): Boolean =
        System.getProperty("os.name").lowercase().contains("win")

    private fun isLinux(): Boolean =
        System.getProperty("os.name").lowercase().contains("linux")

    fun validate(proc: Process): Boolean {
        var brInput: BufferedReader? = null
        var brError: BufferedReader? = null

        try {
            proc.waitFor()
            brInput = BufferedReader(InputStreamReader(proc.inputStream))
            var line: String? = brInput.readLine()
            while (line != null) {
                if (line.contains("unable to connect") || line.contains("cannot connect")) {
                    return false
                }
                line = brInput.readLine()
            }

            brError = BufferedReader(InputStreamReader(proc.errorStream))
            line = brError.readLine()
            while (line != null) {
                if (line.contains("daemon not running; starting now at") ||
                    line.contains("daemon started successfully")) {
                    line = brError.readLine()
                    continue
                }
                if (line.isNotEmpty()) {
                    return false
                }
                line = brError.readLine()
            }

            return true
        } finally {
            brInput?.close()
            brError?.close()
            proc.destroy()
        }
    }

    fun executeAdbConnect(
        host: String,
    ) {
        val runtime = Runtime.getRuntime()

        //adb connect
        val connectCmd: String = java.lang.String.format("adb connect %s", host)
    }

    fun executeAdbBroadcast(
        host: String,
        uuid: String,
        jsonData: String,
        user: String,
        key: String,
    ): Process {
        val base = when {
            isLinux() -> LINUX_RUNTIME
            else -> WIN_RUNTIME
        }

        val broadcastCmd = listOf(
            "${base}adb",
            "-s",
            host,
            "shell",
            "am",
            "broadcast",
            "-a",
            "com.hp.jetadvantage.link.intent.action.ATTESTATION",
            "-n",
            "com.hp.jetadvantage.link.packagemanager/com.hp.jetadvantage.link.pkgmgt.receivers.AttestationChangeBroadcastReceiver",
            "--es",
            "UUID",
            uuid,
            "--es",
            "EXTRA_DATA",
            jsonData,
            "--es",
            "EXTRA_USER",
            user,
            "--es",
            "EXTRA_LDBKEY",
            key
        )

        return ProcessBuilder(broadcastCmd)
            .redirectErrorStream(true)
            .start()
    }
}