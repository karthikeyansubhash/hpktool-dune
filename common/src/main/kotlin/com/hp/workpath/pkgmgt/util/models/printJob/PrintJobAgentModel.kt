package com.hp.workpath.pkgmgt.util.models.printJob

import com.hp.ext.service.printJob.PrintJobAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class PrintJobAgentModel {
    var includePrintJobAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()

    fun from(fromType: PrintJobAgentRegistrationRecord) {
        includePrintJobAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
    }

    fun to(): PrintJobAgentRegistrationRecord {
        return PrintJobAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
        }
    }
}