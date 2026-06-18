package com.hp.workpath.pkgmgt.util.models.authentication

import com.hp.ext.service.authentication.AuthenticationAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.ext.types.authentication.AuthenticationTarget
import com.hp.ext.types.target.AndroidPackageName
import com.hp.ext.types.target.WorkpathPlatformClientTarget
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel

class AuthenticationAgentModel {
    var includeAuthenticationAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()
    var enablePrePrompt: Boolean = false
    var workpathPackage: String = ""
    var enableSignoutNotification: Boolean = false

    fun from(fromType: AuthenticationAgentRegistrationRecord) {
        if (fromType.authenticationTarget.isWorkpath.not()) {
            return
        }
        includeAuthenticationAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
        enablePrePrompt = fromType.enablePrePromptCheck
        workpathPackage = fromType.authenticationTarget.workpath.`package`.toString()
        enableSignoutNotification = fromType.enableSignoutNotification
    }

    fun to(): AuthenticationAgentRegistrationRecord {
        val authenticationAgentRegistrationRecord = AuthenticationAgentRegistrationRecord()
        authenticationAgentRegistrationRecord.agentId = AgentId.createAgentId(agentId)
        authenticationAgentRegistrationRecord.name = AgentName(name)
        authenticationAgentRegistrationRecord.localizedName = title.to()
        authenticationAgentRegistrationRecord.localizedDescription = description.to()
        authenticationAgentRegistrationRecord.enablePrePromptCheck = enablePrePrompt
        authenticationAgentRegistrationRecord.authenticationTarget = AuthenticationTarget(WorkpathPlatformClientTarget().apply {
            `package` = AndroidPackageName(workpathPackage)
        })
        authenticationAgentRegistrationRecord.enableSignoutNotification = enableSignoutNotification
        return authenticationAgentRegistrationRecord
    }
}