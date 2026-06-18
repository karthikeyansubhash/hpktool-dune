package com.hp.workpath.pkgmgt.util.gui.models

import java.nio.file.Path

enum class DataSource {
    DATA_NULL, DATA_FROM_APK, DATA_FROM_USER
}

class ApplicationInfo {
    var applicationId: String = ""
    var applicationName: String = ""
    var applicationPath: String = ""
    var applicationTitle: MutableMap<String, String> = mutableMapOf()
    var applicationTitleSource: DataSource = DataSource.DATA_NULL
    var applicationDescription: MutableMap<String, String> = mutableMapOf()
    var applicationDescriptionSource: DataSource = DataSource.DATA_NULL
    var applicationIconPath: Path? = null
    var applicationIconSource: DataSource = DataSource.DATA_NULL
    var applicationIconSet: MutableMap<String, Path> = mutableMapOf()
    var applicationType: ApplicationType = ApplicationType.MAIN

    enum class ApplicationType {
        MAIN, SUB, HOME, HOME_DEFAULT
    }
}