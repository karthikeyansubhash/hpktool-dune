package com.hp.workpath.pkgmgt.util.models.usbAccessories

import com.hp.ext.service.usbAccessories.ServiceRegistrationTarget
import com.hp.workpath.pkgmgt.util.models.WorkpathPlatformClientTargetModel

class RegistrationTargetModel {
    val workpathPlatformClientTargetModel = WorkpathPlatformClientTargetModel()

    fun from(fromType: ServiceRegistrationTarget) {
        workpathPlatformClientTargetModel.from(fromType.workpath)
    }

    fun to(): ServiceRegistrationTarget {
        return ServiceRegistrationTarget(workpathPlatformClientTargetModel.to())
    }
}