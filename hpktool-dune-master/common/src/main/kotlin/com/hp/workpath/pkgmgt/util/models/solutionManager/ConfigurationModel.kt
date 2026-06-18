package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.types.solutionManager.ConfigurationManifestContent

class ConfigurationModel {
    var includeConfiguration: Boolean = false
    var archiveDataPath: String = ""
    var description: String = ""
    var mimeType: String = ""

    fun from(fromType: ConfigurationManifestContent){
        this.includeConfiguration = true
        this.archiveDataPath = fromType.archiveDataPath
        this.description = fromType.description
        this.mimeType = fromType.mimeType
    }

    fun to(): ConfigurationManifestContent {
        val configurationManifestContent = ConfigurationManifestContent()
        configurationManifestContent.archiveDataPath = this.archiveDataPath
        configurationManifestContent.description = this.description
        configurationManifestContent.mimeType = this.mimeType
        return configurationManifestContent
    }
}