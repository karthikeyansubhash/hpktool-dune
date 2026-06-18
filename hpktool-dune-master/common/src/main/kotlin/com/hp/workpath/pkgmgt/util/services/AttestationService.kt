package com.hp.workpath.pkgmgt.util.services

import com.hp.workpath.pkgmgt.util.models.attestation.AttestationData
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.utilities.*

class AttestationService(
    private val attestation: AttestationData,
    private val taskInterface: TaskInterface
) {

    /**
     * Update attestation data to device
     */
    fun updateAttestation() {
        try {
            // Step 1: ADB Connect
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))

            val connectProcess = ProcessBuilder(ADB, ADB_CMD_CONNECT, attestation.host)
                .redirectErrorStream(true)
                .start()

            connectProcess.waitFor()

            if (connectProcess.exitValue() != 0) {
                val error = connectProcess.inputStream.bufferedReader().readText()
                throw Exception("ADB connect failed: $error")
            }

            val connectOutput = connectProcess.inputStream.bufferedReader().readText()
            println("ADB Connect Success: $connectOutput")

            taskInterface.updateMessage(TaskStatus(TaskState.Sending_Broadcast, ""))

            val jsonData = attestation.attestationData?.toString() ?: "{}"

            var broadcastCmd = "$ADB $ADB_OPT_SERIAL ${attestation.host} $ADB_CMD_SHELL $ADB_CMD_AM $ADB_CMD_BROADCAST " +
                    "-a com.hp.jetadvantage.link.intent.action.ATTESTATION " +
                    "-n com.hp.jetadvantage.link.packagemanager/com.hp.jetadvantage.link.pkgmgt.receivers.AttestationChangeBroadcastReceiver " +
                    "--es UUID ${attestation.uuid} " +
                    "--es EXTRA_DATA '${jsonData}' " +
                    "--es EXTRA_USER '${attestation.userName}' " +
                    "--es EXTRA_LDBKEY '${attestation.key}'"

            if (!attestation.commandData.isNullOrBlank()) {
                broadcastCmd = attestation.commandData + broadcastCmd
            }

            println("ADB Broadcast command: $broadcastCmd")
            val broadcastProcess = ProcessBuilder(broadcastCmd.split(" "))
                .redirectErrorStream(true)
                .start()
            broadcastProcess.waitFor()

            val broadcastOutput = broadcastProcess.inputStream.bufferedReader().readText()
            println("ADB Broadcast Success: $broadcastOutput")

            if (!broadcastOutput.contains(ADB_BROADCAST_SUCCESS_MARKER)) {
                throw Exception("ADB broadcast failed: $broadcastOutput")
            }

            // Success
            taskInterface.updateMessage(TaskStatus(TaskState.Completed, ""))
            taskInterface.onSucceed(null)

        } catch (exception: Exception) {
            taskInterface.updateMessage(TaskStatus(TaskState.Failed, exception.message ?: "Unknown error"))
            taskInterface.onFailed(exception)
        }
    }
}
