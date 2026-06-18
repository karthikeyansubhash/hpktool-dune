package com.hp.workpath.pkgmgt.util.models.scanJob

import com.hp.ext.service.scanJob.ScanJobAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.ext.types.target.WorkpathPlatformClientTarget
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class ScanJobAgentModel {
    var includeScanJobAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()
    val scanNotificationTargetModel = ScanNotificationTargetModel()

    fun from(fromType: ScanJobAgentRegistrationRecord) {
        includeScanJobAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
        scanNotificationTargetModel.from(fromType.scanNotificationTarget)
    }

    fun to(): ScanJobAgentRegistrationRecord {
        return ScanJobAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
            it.scanNotificationTarget = scanNotificationTargetModel.to()
        }
    }
}