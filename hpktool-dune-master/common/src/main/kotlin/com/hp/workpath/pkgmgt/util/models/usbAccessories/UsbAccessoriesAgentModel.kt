package com.hp.workpath.pkgmgt.util.models.usbAccessories

import com.hp.ext.service.usbAccessories.UsbAccessoriesAgentRegistrationRecord
import com.hp.ext.service.usbAccessories.UsbRegistrationIdentification
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.ext.types.target.WorkpathPlatformClientTarget
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class UsbAccessoriesAgentModel {
    var includeUsbAccessoriesAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()
    val registrationTarget = RegistrationTargetModel()
    val registrations = mutableListOf<UsbRegistrationModel>()
    var enablePostRegistrationPromptCheck: Boolean = false

    fun from(fromType: UsbAccessoriesAgentRegistrationRecord) {
        includeUsbAccessoriesAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        description.from(fromType.localizedDescription)
        registrationTarget.from(fromType.registrationTarget)
        fromType.registrations.forEach {
            registrations.add(UsbRegistrationModel().apply { from(it) })
        }
        enablePostRegistrationPromptCheck = false
    }

    fun to(): UsbAccessoriesAgentRegistrationRecord {
        return UsbAccessoriesAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.name = AgentName(name)
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.registrationTarget = registrationTarget.to()
            it.registrations = mutableListOf<UsbRegistrationIdentification>()
            registrations.forEach { registration ->
                it.registrations.add(registration.to())
            }
            it.enablePostRegistrationPromptCheck = false
        }
    }
}