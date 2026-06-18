package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.types.solutionManager.WorkpathPlatformPackage
import com.hp.workpath.pkgmgt.util.models.hpk.WorkpathPlatformVersion
import com.hp.workpath.pkgmgt.util.utilities.LATEST_PLATFORM_VERSION

class WorkpathPlatformPackageModel {
    var workpathPackagePath: String = ""
    var platformVersion: WorkpathPlatformVersion = LATEST_PLATFORM_VERSION
    var installFile: String = ""    //from xml

    fun from(fromType: WorkpathPlatformPackage) {
        this.workpathPackagePath = fromType.workpathPackagePath
        if (fromType.platformVersion.isNotEmpty()) { // TODO after beta7
            this.platformVersion = WorkpathPlatformVersion.getEnumByValue(fromType.platformVersion)
        }
    }

    fun to(): WorkpathPlatformPackage {
        val workpathPlatformPackage = WorkpathPlatformPackage()
        workpathPlatformPackage.workpathPackagePath = this.workpathPackagePath
        workpathPlatformPackage.platformVersion = this.platformVersion.toString()
        return workpathPlatformPackage
    }
}