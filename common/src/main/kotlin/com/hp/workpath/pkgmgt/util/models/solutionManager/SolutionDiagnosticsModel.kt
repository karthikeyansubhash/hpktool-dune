package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.service.printJob.PrintJobAgentRegistrationRecord
import com.hp.ext.service.solutionDiagnostics.SolutionDiagnosticsAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class SolutionDiagnosticsModel {
    var includeSolutionDiagnosticsAgent: Boolean = true
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()

    fun from(fromType: SolutionDiagnosticsAgentRegistrationRecord) {
        includeSolutionDiagnosticsAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
    }

    fun to(): SolutionDiagnosticsAgentRegistrationRecord {
        return SolutionDiagnosticsAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
        }
    }
}