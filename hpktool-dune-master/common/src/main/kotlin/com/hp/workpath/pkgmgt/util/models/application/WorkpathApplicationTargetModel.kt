package com.hp.workpath.pkgmgt.util.models.application

import com.hp.ext.types.application.WorkpathApplicationTarget
import com.hp.ext.types.target.AndroidPackageName

class WorkpathApplicationTargetModel {
    var isMainApplication: Boolean = false
    var workpathPackage: String = ""

    fun from(fromType: WorkpathApplicationTarget) {
        if (fromType.`package`.toString().startsWith("main=")) {
            isMainApplication = true
            workpathPackage = fromType.`package`.toString().removePrefix("main=")
        } else if (fromType.`package`.toString().startsWith("sub=")) {
            isMainApplication = false
            workpathPackage = fromType.`package`.toString().removePrefix("sub=")
        }
    }

    fun to(): WorkpathApplicationTarget {
        val workpathApplicationTarget = WorkpathApplicationTarget()
        workpathApplicationTarget.`package` =
            AndroidPackageName((if (isMainApplication) "main=" else "sub=") + workpathPackage)
        return workpathApplicationTarget
    }
}