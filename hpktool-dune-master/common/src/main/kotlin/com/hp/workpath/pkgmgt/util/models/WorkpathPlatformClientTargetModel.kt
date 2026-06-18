package com.hp.workpath.pkgmgt.util.models

import com.hp.ext.types.target.AndroidPackageName
import com.hp.ext.types.target.WorkpathPlatformClientTarget

class WorkpathPlatformClientTargetModel {
    var androidPackageName: String = ""

    fun from(fromType: WorkpathPlatformClientTarget) {
        androidPackageName = fromType.`package`.toString()
    }

    fun to(): WorkpathPlatformClientTarget {
        return WorkpathPlatformClientTarget().also {
            it.`package` = AndroidPackageName(androidPackageName)
        }
    }
}