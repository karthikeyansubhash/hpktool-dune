package com.hp.workpath.pkgmgt.util.models.copyJob

import com.hp.ext.service.copy.CopyNotificationTarget
import com.hp.workpath.pkgmgt.util.models.WorkpathPlatformClientTargetModel

class CopyNotificationTargetModel {
    var workpathPlatformClientTargetModel = WorkpathPlatformClientTargetModel()

    fun from(fromType: CopyNotificationTarget) {
        workpathPlatformClientTargetModel.from(fromType.workpath)
    }

    fun to(): CopyNotificationTarget {
        return CopyNotificationTarget(workpathPlatformClientTargetModel.to())
    }
}