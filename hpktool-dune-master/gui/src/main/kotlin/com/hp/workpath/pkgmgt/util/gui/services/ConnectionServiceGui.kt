package com.hp.workpath.pkgmgt.util.gui.services

import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.services.ConnectionService
import com.hp.workpath.pkgmgt.util.utilities.TaskInterface
import javafx.concurrent.Service
import javafx.concurrent.Task

enum class ConnectionServiceType {
    INSTALL, UNINSTALL, ENUMERATE_SOLUTIONS, GET_SOLUTION, CONFIG_GET, CONFIG_UPDATE, ATTESTATION_UPDATE,
}

class ConnectionServiceGui(
    connectionData: ConnectionData,
    taskInterface: TaskInterface,
    private val connectionServiceType: ConnectionServiceType,
) : Service<Boolean>() {
    private var connectionService: ConnectionService

    init {
        connectionService = ConnectionService(connectionData, taskInterface)
    }

    fun execute() {
        when (connectionServiceType) {
            ConnectionServiceType.INSTALL -> connectionService.installSolution()
            ConnectionServiceType.UNINSTALL -> connectionService.uninstallSolution()
            ConnectionServiceType.ENUMERATE_SOLUTIONS -> connectionService.enumerateSolutions()
            ConnectionServiceType.GET_SOLUTION -> connectionService.getSolution()
            ConnectionServiceType.CONFIG_GET -> connectionService.getConfigurationData()
            ConnectionServiceType.CONFIG_UPDATE -> connectionService.updateConfigurationData()
            ConnectionServiceType.ATTESTATION_UPDATE -> connectionService.updateAttestationData()
        }
    }

    override fun createTask(): Task<Boolean> {
        return object : Task<Boolean>() {
            override fun call(): Boolean? {
                execute()
                return null
            }
        }
    }
}