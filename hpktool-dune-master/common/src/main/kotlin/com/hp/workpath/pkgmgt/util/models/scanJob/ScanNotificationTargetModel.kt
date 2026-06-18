package com.hp.workpath.pkgmgt.util.models.scanJob

import com.hp.ext.service.scanJob.ScanNotificationTarget
import com.hp.workpath.pkgmgt.util.models.WorkpathPlatformClientTargetModel

class ScanNotificationTargetModel {
    var workpathPlatformClientTargetModel = WorkpathPlatformClientTargetModel()

    fun from(fromType: ScanNotificationTarget) {
        workpathPlatformClientTargetModel.from(fromType.workpath)
    }

    fun to(): ScanNotificationTarget {
        return ScanNotificationTarget(workpathPlatformClientTargetModel.to())
    }
}