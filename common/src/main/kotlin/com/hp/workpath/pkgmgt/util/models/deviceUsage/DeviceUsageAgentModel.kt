package com.hp.workpath.pkgmgt.util.models.deviceUsage

import com.hp.ext.service.deviceUsage.DeviceUsageAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class DeviceUsageAgentModel {
    var includeDeviceUsageAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()

    fun from(fromType: DeviceUsageAgentRegistrationRecord) {
        includeDeviceUsageAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
    }

    fun to(): DeviceUsageAgentRegistrationRecord {
        return DeviceUsageAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
        }
    }
}