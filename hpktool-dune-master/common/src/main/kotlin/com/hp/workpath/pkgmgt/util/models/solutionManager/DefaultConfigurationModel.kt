package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.types.solutionManager.ConfigurationManifestContent

// FW features for default configuration like get, update, etc. will be updated after beta8, maybe after beta9, 10.
class DefaultConfigurationModel {
    var includeConfiguration: Boolean = false
    var archiveDataPath: String = ""
    var mimeType: String = ""
    var description: String = ""

    fun from(fromType: ConfigurationManifestContent) {
        this.archiveDataPath = fromType.archiveDataPath
        this.mimeType = fromType.mimeType
        this.description = fromType.description
    }

    fun to(): ConfigurationManifestContent {
        val defaultConfigurationManifestContent = ConfigurationManifestContent()
        defaultConfigurationManifestContent.archiveDataPath = this.archiveDataPath
        defaultConfigurationManifestContent.mimeType = this.mimeType
        defaultConfigurationManifestContent.description = this.description
        return defaultConfigurationManifestContent
    }
}