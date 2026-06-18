package com.hp.workpath.pkgmgt.util.models.messageCenter

import com.hp.ext.service.application.MessageCenterAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class MessageCenterAgentModel {
    var includeMessageCenterAgent: Boolean = true
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()

    fun from(fromType: MessageCenterAgentRegistrationRecord) {
        includeMessageCenterAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
    }

    fun to(): MessageCenterAgentRegistrationRecord {
        return MessageCenterAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
        }
    }
}