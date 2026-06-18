package com.hp.workpath.pkgmgt.util.models.statisticsJob

import com.hp.ext.service.jobStatistics.JobStatisticsAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel
import java.util.*

class StatisticsJobAgentModel {
    var includeStatisticsJobAgent: Boolean = false
    var agentId: String = ""
    var name: String = ""
    val title = LocalizedStringReferenceModel()
    val description = LocalizedStringReferenceModel()
    var criticalSolution: Boolean = false
    val defaultNotificationFilter = ContentFilterModel()
    val statisticsNotificationTargetModel = StatisticsNotificationTargetModel()

    fun from(fromType: JobStatisticsAgentRegistrationRecord) {
        includeStatisticsJobAgent = true
        agentId = fromType.agentId.toString()
        name = fromType.name.toString()
        title.from(fromType.localizedName)
        criticalSolution = fromType.criticalSolution
        description.from(fromType.localizedDescription)
        defaultNotificationFilter.from(fromType.defaultNotificationFilter)
        statisticsNotificationTargetModel.from(fromType.notificationTarget)
    }

    fun to() : JobStatisticsAgentRegistrationRecord {
        return JobStatisticsAgentRegistrationRecord().also {
            it.agentId = AgentId(UUID.fromString(agentId))
            it.localizedName = title.to()
            it.localizedDescription = description.to()
            it.name = AgentName(name)
            it.criticalSolution = criticalSolution
            it.defaultNotificationFilter = defaultNotificationFilter.to()
            it.notificationTarget = statisticsNotificationTargetModel.to()
        }
    }

}