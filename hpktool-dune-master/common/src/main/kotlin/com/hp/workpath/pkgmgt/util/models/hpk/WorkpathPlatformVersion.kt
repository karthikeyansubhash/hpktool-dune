package com.hp.workpath.pkgmgt.util.models.hpk

import com.hp.workpath.pkgmgt.util.utilities.LATEST_PLATFORM_VERSION

enum class WorkpathPlatformVersion(private val platformVersion: String, val hpkVersion: HPKVersion) {
    WORKPATH_PLATFORM_19_3("19.3", HPKVersion.HPK_1_3),
    WORKPATH_PLATFORM_19_4("19.4", HPKVersion.HPK_1_3),
    WORKPATH_PLATFORM_26_4("26.4", HPKVersion.HPK_1_3),
    WORKPATH_PLATFORM_29_4("29.4", HPKVersion.HPK_1_3),
    WORKPATH_PLATFORM_29_5("29.5", HPKVersion.HPK_1_4),
    WORKPATH_PLATFORM_29_6("29.6", HPKVersion.HPK_1_4),
    WORKPATH_PLATFORM_29_7("29.7", HPKVersion.HPK_1_4),
    WORKPATH_PLATFORM_31_7("31.7", HPKVersion.HPK_1_4),
    WORKPATH_PLATFORM_31_8("31.8", HPKVersion.HPK_1_4),
    WORKPATH_PLATFORM_31_9("31.9", HPKVersion.HPK_1_5);
    /**
     * checkPlatformVersion
     * @param hpkVersion
     * @return true if input hpkVersion is less than HPK 1.3 or LinkPlatformVersion and HpkVersion are matched.
     */
    fun checkPlatformVersion(hpkVersion: HPKVersion): Boolean {
        return if (hpkVersion.level < HPKVersion.HPK_1_3.level) {
            true
        } else this.hpkVersion == hpkVersion
    }

    override fun toString(): String {
        return platformVersion
    }

    companion object {
        /**
         * getEnumByValue
         * @param value platformVersion String
         * @return LinkPlatformVersion that matches with input string
         */
        fun getEnumByValue(value: String): WorkpathPlatformVersion {
            for (workpathPlatformVersion in WorkpathPlatformVersion.values()) {
                if (workpathPlatformVersion.toString().equals(value, ignoreCase = true))
                    return workpathPlatformVersion
            }
            throw IllegalArgumentException("Failed: Input version ($value) not supported. Maximum supported version is $LATEST_PLATFORM_VERSION.")
        }

        /**
         * getEnumByHPKVersion
         * @param hpkVersion
         * @return Find latest LinkPlatformVersion using input hpkVersion
         */
        fun getEnumByHPKVersion(hpkVersion: HPKVersion): WorkpathPlatformVersion? {
            if (hpkVersion.level < HPKVersion.HPK_1_3.level) {
                return null
            }
            var ret: WorkpathPlatformVersion? = WORKPATH_PLATFORM_19_3
            for (workpathPlatformVersion in WorkpathPlatformVersion.values()) {
                if (workpathPlatformVersion.hpkVersion.level == hpkVersion.level) {
                    ret = workpathPlatformVersion
                }
            }
            return ret
        }
    }
}