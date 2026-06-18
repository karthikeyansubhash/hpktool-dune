package com.hp.workpath.pkgmgt.util.models.application

import com.hp.ext.service.application.ApplicationAgentRegistrationRecord
import com.hp.ext.types.agent.AgentId
import com.hp.ext.types.agent.AgentName
import com.hp.ext.types.application.ApplicationCategory
import com.hp.ext.types.application.ApplicationPlatform
import com.hp.ext.types.application.ApplicationTarget
import com.hp.ext.types.application.KeyedApplicationImage
import com.hp.workpath.pkgmgt.util.models.application.ApplicationCategory.*
import com.hp.workpath.pkgmgt.util.utilities.EXCEPTION_OUT_OF_INDEX
import java.util.*

class ApplicationAgentModel {
    val details = ApplicationAgentDetailsModel()
    val target = WorkpathApplicationTargetModel()
//    val failureBehavior = ApplicationFailureBehaviorModel()
//    val intentProfile = IntentProfileModel()

    fun from(fromType: ApplicationAgentRegistrationRecord) {
        details.applicationId = fromType.agentId.toString()
        details.name = fromType.name.toString()
        details.platform = fromType.platform.toString()
        details.category = when (fromType.category) {
            ApplicationCategory.AcStandardWalkup -> Standard
            ApplicationCategory.AcKiosk -> HomeScreen
            else -> throw Exception("${javaClass.simpleName} $EXCEPTION_OUT_OF_INDEX")
        }
        details.description.from(fromType.localizedDescription)
        details.title.from(fromType.localizedName)
        details.icon.from(fromType.icon)
        fromType.iconSet?.forEach { icons ->
            details.iconSet.add(ApplicationIconDetailModel().apply {
                from(icons.icon)
                key = icons.key
                isInIconSet = true
            })
        }
        target.from(fromType.target.workpathApplicationTarget)
    }

    fun to(): ApplicationAgentRegistrationRecord {
        val applicationAgentRegistrationRecord = ApplicationAgentRegistrationRecord()
        applicationAgentRegistrationRecord.agentId = AgentId(UUID.fromString(details.applicationId))
        applicationAgentRegistrationRecord.name = AgentName(details.name)
        applicationAgentRegistrationRecord.platform = ApplicationPlatform.ApWorkpath
        applicationAgentRegistrationRecord.category =
            when (details.category) {
                Standard -> {
                    ApplicationCategory.AcStandardWalkup
                }
                HomeScreen -> {
                    ApplicationCategory.AcKiosk
                }
                else -> {
                    throw Exception("${javaClass.simpleName} $EXCEPTION_OUT_OF_INDEX")
                }
            }
        applicationAgentRegistrationRecord.localizedTitle = details.title.to()
        applicationAgentRegistrationRecord.localizedDescription = details.description.to()
        // comment from e2
        // Due to using AgentTypes, we also have LocalizedName. Rather than add a duplicate field
        // in the GUI we will just make LocalizedName be a copy of LocalizedTitle
        applicationAgentRegistrationRecord.localizedName = details.title.to()
        applicationAgentRegistrationRecord.icon = details.icon.to()
        applicationAgentRegistrationRecord.iconSet = mutableListOf<KeyedApplicationImage>()
        for(icons in details.iconSet) {
            if (!icons.isInIconSet) {
                throw Exception("This is not a iconset icon: ${icons.localIcon.originalPath}")
            }
            applicationAgentRegistrationRecord.iconSet.add(KeyedApplicationImage().apply {
                icon = icons.to()
                key = icons.key
            })
        }
        applicationAgentRegistrationRecord.target = ApplicationTarget().apply {
            workpathApplicationTarget = target.to()
        }
        applicationAgentRegistrationRecord.failureBehavior = null
        applicationAgentRegistrationRecord.intentProfile = null
        return applicationAgentRegistrationRecord
    }
}