package com.hp.workpath.pkgmgt.util.models.hpk

import com.hp.workpath.pkgmgt.util.utilities.LATEST_PLATFORM_VERSION

enum class HPKVersion(private val hpkVersion: String, val level: Int, val xsdPath: String) {
    HPK_1_0("v2.1", 1, "/hpk.xsd"),
    HPK_1_1("v2.2", 2, "/hpk_2.2.xsd"),
    HPK_1_2("v2.3", 3, "/hpk_2.3.xsd"),
    HPK_1_3("v2.4", 4, "/hpk_2.4.xsd"),
    HPK_1_4("v2.5", 5, "/hpk_2.5.xsd"),
    HPK_1_5("v2.6", 6, "/hpk_2.6xsd");

    override fun toString(): String {
        return hpkVersion
    }

    companion object {
        fun getHPKVersion(versionStr: String): HPKVersion {
            for (version in HPKVersion.values()) {
                if (versionStr.lowercase().equals(version.hpkVersion.lowercase(), ignoreCase = true)) {
                    return version
                }
            }
            throw IllegalArgumentException("Failed: Input version ($versionStr) not supported. Maximum supported version is $LATEST_PLATFORM_VERSION.")
        }
    }
}