package com.hp.workpath.pkgmgt.util.services

import com.hp.ext.service.solutionManager.Data
import com.hp.ext.service.solutionManager.InstallSolutionRequest
import com.hp.ext.service.solutionManager.Solution
import com.hp.ext.service.solutionManager.Solutions
import com.hp.ext.service.solutionManager.UninstallSolutionRequest
import com.hp.ext.types.agent.SolutionId
import com.hp.ext.types.solutionManager.InstallerOperationState
import com.hp.ext.types.solutionManager.InstallerStatus
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.services.deviceManagement.DeviceManagementServiceDefault
import com.hp.workpath.pkgmgt.util.services.solutionManager.SolutionManagerServiceDefault
import com.hp.workpath.pkgmgt.util.utilities.ACCOUNT_ADMIN
import com.hp.workpath.pkgmgt.util.utilities.ADB
import com.hp.workpath.pkgmgt.util.utilities.ADB_BROADCAST_SUCCESS_MARKER
import com.hp.workpath.pkgmgt.util.utilities.ADB_CMD_AM
import com.hp.workpath.pkgmgt.util.utilities.ADB_CMD_BROADCAST
import com.hp.workpath.pkgmgt.util.utilities.ADB_CMD_CONNECT
import com.hp.workpath.pkgmgt.util.utilities.ADB_CMD_SHELL
import com.hp.workpath.pkgmgt.util.utilities.ADB_OPT_SERIAL
import com.hp.workpath.pkgmgt.util.utilities.CONNECTION_TRIES_DELAY
import com.hp.workpath.pkgmgt.util.utilities.CONNECTION_TRIES_MAX
import com.hp.workpath.pkgmgt.util.utilities.DEFAULT_CONFIG_KEY
import com.hp.workpath.pkgmgt.util.utilities.DEFAULT_CONFIG_MIME
import com.hp.workpath.pkgmgt.util.utilities.EXCEPTION_CONNECTION_TIMEOUT
import com.hp.workpath.pkgmgt.util.utilities.EXCEPTION_DEVICE_BUSY
import com.hp.workpath.pkgmgt.util.utilities.InsecureHttpClientFactory
import com.hp.workpath.pkgmgt.util.utilities.TaskInterface
import com.hp.workpath.pkgmgt.util.utilities.TaskStatus
import kotlin.io.path.inputStream
import kotlin.io.path.name

class ConnectionService(private val connectionData: ConnectionData, private val taskInterface: TaskInterface) {
    private val httpClientFactory = InsecureHttpClientFactory()
    private val deviceManagementService = DeviceManagementServiceDefault(httpClientFactory)

    fun installSolution() {
        try {
            // bind device
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))
            deviceManagementService.bindDevice(connectionData.networkAddress)
            deviceManagementService.getServicesDiscovery()
            // get oauth2 token
            taskInterface.updateMessage(TaskStatus(TaskState.Authorizing, ""))
            deviceManagementService.passwordGrant(ACCOUNT_ADMIN, connectionData.password)
            // check install status
            taskInterface.updateMessage(TaskStatus(TaskState.CheckingStatus, ""))
            val solutionManagementService =
                SolutionManagerServiceDefault(deviceManagementService, httpClientFactory)
            if (solutionManagementService.installer.status != InstallerStatus.IsIdle) {
                throw RuntimeException(EXCEPTION_DEVICE_BUSY)
            }
            // do install
            taskInterface.updateMessage(TaskStatus(TaskState.Sending, ""))

            val installRequest = InstallSolutionRequest()
            val fileInputStream = connectionData.installFilePath.inputStream()
            val fileName = connectionData.installFilePath.name
            val installerInstall = solutionManagementService.installSolution(
                installRequest,
                fileInputStream,
                fileName,
                null
            )
            val operationId = installerInstall.installerOperation.operationId

            // check install status
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, "$operationId"))
            for (time in 1..CONNECTION_TRIES_MAX) {
                Thread.sleep(CONNECTION_TRIES_DELAY)
                val installerOperation = solutionManagementService.getInstallerOperation(operationId.toString())
                if (installerOperation.operationState != InstallerOperationState.IosInProgress) {
                    if (installerOperation.operationState == InstallerOperationState.IosSucceeded) {
                        connectionData.solutionId = installerOperation.solutionId.toString()
                        taskInterface.onSucceed(connectionData.solutionId)
                        return
                    } else {
                        throw RuntimeException(
                            if (installerOperation.installEventDetails.eventDescription.isNullOrEmpty()) {
                                installerOperation.operationState.toString().substring(3)
                            } else {
                                installerOperation.installEventDetails.eventDescription
                            }
                        )
                    }
                }
            }
            throw RuntimeException(EXCEPTION_CONNECTION_TIMEOUT)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun uninstallSolution() {
        try {
            // bind device
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))
            deviceManagementService.bindDevice(connectionData.networkAddress)
            deviceManagementService.getServicesDiscovery()
            // get oauth2 token
            taskInterface.updateMessage(TaskStatus(TaskState.Authorizing, ""))
            deviceManagementService.passwordGrant(ACCOUNT_ADMIN, connectionData.password)
            // check install status
            taskInterface.updateMessage(TaskStatus(TaskState.CheckingStatus, ""))
            val solutionManagementService =
                SolutionManagerServiceDefault(deviceManagementService, httpClientFactory)
            if (solutionManagementService.installer.status != InstallerStatus.IsIdle) {
                throw RuntimeException(EXCEPTION_DEVICE_BUSY)
            }
            // do uninstall
            taskInterface.updateMessage(TaskStatus(TaskState.Sending, ""))

            val uninstallRequest = UninstallSolutionRequest()
            val solutionIdString = connectionData.solutionId
            val solutionId = SolutionId.createSolutionId(solutionIdString)
            uninstallRequest.solutionId = solutionId
            val uninstallOperation = solutionManagementService.uninstallSolution(uninstallRequest)

            val operationId = uninstallOperation.installerOperation.operationId
            // check install status
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, "$operationId"))
            for (time in 1..CONNECTION_TRIES_MAX) {
                Thread.sleep(CONNECTION_TRIES_DELAY)
                val installerOperation = solutionManagementService.getInstallerOperation(operationId.toString())
                if (installerOperation.operationState != InstallerOperationState.IosInProgress) {
                    if (installerOperation.operationState == InstallerOperationState.IosSucceeded) {
                        taskInterface.onSucceed(connectionData.solutionId)
                        return
                    } else {
                        throw RuntimeException(
                            if (installerOperation.installEventDetails.eventDescription.isNullOrEmpty()) {
                                installerOperation.operationState.toString().substring(3)
                            } else {
                                installerOperation.installEventDetails.eventDescription
                            }
                        )
                    }
                }
            }
            throw RuntimeException(EXCEPTION_CONNECTION_TIMEOUT)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun enumerateSolutions() {
        try {
            // bind device
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))
            deviceManagementService.bindDevice(connectionData.networkAddress)
            deviceManagementService.getServicesDiscovery()
            // get oauth2 token
            taskInterface.updateMessage(TaskStatus(TaskState.Authorizing, ""))
            deviceManagementService.passwordGrant(ACCOUNT_ADMIN, connectionData.password)
            // get solutions
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, ""))
            val solutionManagerService =
                SolutionManagerServiceDefault(
                    deviceManagementService,
                    httpClientFactory
                )
            val solutions: Solutions = solutionManagerService.enumerateSolutions(
                true,
                "[\"members/(solutionId, description, installationDetails, packages)\", \"memberIds\", \"offset\", \"selectedCount\", \"totalCount\"]"
            )
            // TODO handle packages is workpath or not.
            taskInterface.onSucceed(solutions)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun getSolution() {
        try {
            // bind device
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))
            deviceManagementService.bindDevice(connectionData.networkAddress)
            deviceManagementService.getServicesDiscovery()
            // get oauth2 token
            taskInterface.updateMessage(TaskStatus(TaskState.Authorizing, ""))
            deviceManagementService.passwordGrant(ACCOUNT_ADMIN, connectionData.password)
            // get solution
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, ""))
            val solutionManagerService =
                SolutionManagerServiceDefault(
                    deviceManagementService,
                    httpClientFactory
                )
            val solution: Solution = solutionManagerService.getSolution(connectionData.solutionId)
            taskInterface.onSucceed(solution)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun getConfigurationData() {
        try {
            // bind device
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))
            deviceManagementService.bindDevice(connectionData.networkAddress)
            deviceManagementService.getServicesDiscovery()
            // get oauth2 token
            taskInterface.updateMessage(TaskStatus(TaskState.Authorizing, ""))
            deviceManagementService.passwordGrant(ACCOUNT_ADMIN, connectionData.password)
            // get data
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, ""))
            val solutionManagerService = SolutionManagerServiceDefault(deviceManagementService, httpClientFactory)
            val map: Map<String, Any>? = solutionManagerService.getConfigurationData(connectionData.solutionId, DEFAULT_CONFIG_KEY)
            taskInterface.onSucceed(map)
        } catch(exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun updateConfigurationData() {
        try{
            // bind device
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))
            deviceManagementService.bindDevice(connectionData.networkAddress)
            deviceManagementService.getServicesDiscovery()
            // get oauth2 token
            taskInterface.updateMessage(TaskStatus(TaskState.Authorizing, ""))
            deviceManagementService.passwordGrant(ACCOUNT_ADMIN, connectionData.password)
            // update data
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, ""))
            val solutionManagerService = SolutionManagerServiceDefault(deviceManagementService, httpClientFactory)
            val data: Data? = solutionManagerService.replaceConfigurationData(connectionData.solutionId, DEFAULT_CONFIG_KEY, connectionData.configData.byteInputStream(), DEFAULT_CONFIG_MIME)
            taskInterface.updateMessage(TaskStatus(TaskState.CheckingStatus, ""))
            val map: Map<String, Any>? = solutionManagerService.getConfigurationData(connectionData.solutionId, DEFAULT_CONFIG_KEY)
            taskInterface.onSucceed(map)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun updateAttestationData() {
        try{
            // bind device
            taskInterface.updateMessage(TaskStatus(TaskState.Connecting, ""))
            deviceManagementService.bindDevice(connectionData.networkAddress)
            deviceManagementService.getServicesDiscovery()
            // get oauth2 token
            taskInterface.updateMessage(TaskStatus(TaskState.Authorizing, ""))
            deviceManagementService.passwordGrant(ACCOUNT_ADMIN, connectionData.password)
            // update data
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, ""))

            val connectProcess = ProcessBuilder(ADB, ADB_CMD_CONNECT, connectionData.networkAddress)
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

            val jsonData = connectionData.attestationData ?: "{}"

            var broadcastCmd = "$ADB $ADB_OPT_SERIAL ${connectionData.networkAddress} $ADB_CMD_SHELL $ADB_CMD_AM $ADB_CMD_BROADCAST " +
                    "-a com.hp.jetadvantage.link.intent.action.ATTESTATION " +
                    "-n com.hp.jetadvantage.link.packagemanager/com.hp.jetadvantage.link.pkgmgt.receivers.AttestationChangeBroadcastReceiver " +
                    "--es UUID ${connectionData.solutionId} " +
                    "--es EXTRA_DATA '${jsonData}' " +
                    "--es EXTRA_USER '${connectionData.userName}' " +
                    "--es EXTRA_LDBKEY '${connectionData.key}'"

            if (!connectionData.commandData.isNullOrBlank()) {
                broadcastCmd = connectionData.commandData + broadcastCmd
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
            taskInterface.onFailed(exception)
        }
    }
}
