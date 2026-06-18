package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.service.solutionManager.SolutionNotificationAgentRegistrationRecord
import com.hp.ext.service.solutionManager.SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Binding
import com.hp.ext.service.solutionManager.SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Value
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.ext.types.solutionManager.NotificationType
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class SolutionNotificationAgentModel {
    var includeNotificationAgent : Boolean = true
    var agentId : String = ""
    val notificationTargetModel = SolutionNotificationTargetModel()
    val notificationsToReceiveModel = NotificationsToReceiveModel()

    fun from(fromType: SolutionNotificationAgentRegistrationRecord) {
        includeNotificationAgent = true
        agentId = fromType.agentId.toString()
        notificationTargetModel.from(fromType.notificationTarget)
        notificationsToReceiveModel.from(fromType.notificationsToReceive)
    }

    fun to(): SolutionNotificationAgentRegistrationRecord {
        return SolutionNotificationAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.notificationTarget = notificationTargetModel.to()
            it.notificationsToReceive = notificationsToReceiveModel.to()
        }
    }

}

class NotificationsToReceiveModel {
    var explict = NotificationToReceiveValueModel()

    fun from(fromType: SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Binding) {
        explict.from(fromType.explicit)
    }

    fun to() : SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Binding{
        return SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Binding(explict.to())
    }
}

class NotificationToReceiveValueModel {
    var explicitValue: MutableList<NotificationType> = mutableListOf()

    fun from(fromType: SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Value) {
        explicitValue = fromType.explicitValue
    }

    fun to() : SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Value {
        return SolutionNotificationAgentRegistrationRecord_NotificationsToReceive_Value().also {
            it.explicitValue = explicitValue
        }
    }
}