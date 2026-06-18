package com.hp.workpath.pkgmgt.util.models.copyJob

import com.hp.ext.service.copy.CopyAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class CopyJobAgentModel {
    var includeCopyJobAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()
    val copyNotificationTargetModel = CopyNotificationTargetModel()

    fun from(fromType: CopyAgentRegistrationRecord) {
        includeCopyJobAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
        copyNotificationTargetModel.from(fromType.copyNotificationTarget)
    }

    fun to(): CopyAgentRegistrationRecord {
        return CopyAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
            it.copyNotificationTarget = copyNotificationTargetModel.to()
        }
    }
}