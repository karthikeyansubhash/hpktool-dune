package com.hp.workpath.pkgmgt.util.models.supplies

import com.hp.ext.service.supplies.SuppliesAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class SuppliesAgentModel {
    var includeSuppliesAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()

    fun from(fromType: SuppliesAgentRegistrationRecord) {
        includeSuppliesAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
    }

    fun to(): SuppliesAgentRegistrationRecord {
        return SuppliesAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
        }
    }
}