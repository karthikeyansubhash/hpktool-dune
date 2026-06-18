package com.hp.workpath.pkgmgt.util.models.statisticsJob

import com.hp.ext.service.jobStatistics.RegistrationTarget
import com.hp.workpath.pkgmgt.util.models.WorkpathPlatformClientTargetModel

class StatisticsNotificationTargetModel {
    var workpathPlatformClientTargetModel = WorkpathPlatformClientTargetModel()

    fun from(fromType: RegistrationTarget) {
        workpathPlatformClientTargetModel.from(fromType.workpath)
    }

    fun to(): RegistrationTarget {
        return RegistrationTarget(workpathPlatformClientTargetModel.to())
    }
}