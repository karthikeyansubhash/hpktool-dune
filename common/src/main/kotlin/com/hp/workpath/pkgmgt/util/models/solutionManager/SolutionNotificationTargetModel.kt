package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.types.solutionManager.NotificationTarget
import com.hp.workpath.pkgmgt.util.models.WorkpathPlatformClientTargetModel

class SolutionNotificationTargetModel {
    var workpathPlatformClientTargetModel = WorkpathPlatformClientTargetModel()

    fun from(fromType: NotificationTarget) {
        workpathPlatformClientTargetModel.from(fromType.workpath)
    }

    fun to(): NotificationTarget {
        return NotificationTarget(workpathPlatformClientTargetModel.to())
    }
}