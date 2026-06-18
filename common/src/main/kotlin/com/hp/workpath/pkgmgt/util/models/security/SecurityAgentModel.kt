package com.hp.workpath.pkgmgt.util.models.security

import com.hp.ext.service.security.SecurityAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class SecurityAgentModel {
    var includeSecurityAgent: Boolean = true
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()
    var declaredExpressionOperators: MutableList<String> = mutableListOf()
    var securityContextExpressionsEnabled: Boolean = false


    fun from(fromType: SecurityAgentRegistrationRecord) {
        includeSecurityAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
        declaredExpressionOperators = fromType.declaredExpressionOperators
        //securityContextExpressionsEnabled = fromType.securityContextExpressionsEnabled

    }

    fun to(): SecurityAgentRegistrationRecord {
        return SecurityAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
            it.declaredExpressionOperators = declaredExpressionOperators
            //it.securityContextExpressionsEnabled = securityContextExpressionsEnabled
        }
    }
}